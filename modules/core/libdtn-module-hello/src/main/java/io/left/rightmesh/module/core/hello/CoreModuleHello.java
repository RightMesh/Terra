package io.left.rightmesh.module.core.hello;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBlob;
import io.left.rightmesh.libdtn.common.data.blob.WritableBlob;
import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;

/**
 * <p>CoreHelloModule is a Core Module that reacts to new peer event by sending hello message
 * containing the local Eid of the current node. </p>
 *
 * <p>When an Hello message is received by a peer, it updates the Routing Table and add an entry
 * matching the received peer local Eid with the ClaEid this hello message was received from.</p>
 *
 * @author Lucien Loiseau on 13/11/18.
 */
public class CoreModuleHello implements CoreModuleSPI {

    private static final String TAG = "HelloModule";

    private static class RequestException extends Exception {
        RequestException(String msg) {
            super("Request: " + msg);
        }
    }

    private CoreAPI coreAPI;
    private Bundle helloBundle;

    public CoreModuleHello() {
    }

    @Override
    public String getModuleName() {
        return "hello";
    }

    private void prepareHelloBundle() {
        HelloMessage hello = new HelloMessage(coreAPI.getExtensionManager().getEidFactory());

        /* add node local Eid */
        hello.eids.add(coreAPI.getLocalEID().localEID());

        /* add aliases */
        hello.eids.addAll(coreAPI.getLocalEID().aliases());

        /* get size of hello message for the payload */
        long size = hello.encode().observe()
                .map(ByteBuffer::remaining)
                .reduce(0, (a, b) -> a + b)
                .blockingGet();

        /* serialize the hello message into a Blob (for the payload) */
        UntrackedByteBufferBlob blobHello = new UntrackedByteBufferBlob((int) size);
        final WritableBlob wblob = blobHello.getWritableBlob();
        hello.encode().observe()
                .map(wblob::write)
                .doOnComplete(wblob::close)
                .subscribe();

        /* create Hello Bundle Skeleton */
        helloBundle = new Bundle(DtnEid.nullEid());
        helloBundle.addBlock(new PayloadBlock(blobHello));
    }

    @Override
    public void init(CoreAPI api) {
        this.coreAPI = api;

        prepareHelloBundle();

        try {
            api.getRegistrar().register("/hello/", (bundle) -> {
                if (bundle.getTagAttachment("cla-origin-iid") != null) {
                    coreAPI.getLogger().i(TAG, "received hello message from: " +
                            bundle.getSource().getEidString() +
                            " on BaseClaEid: " +
                            bundle.<Eid>getTagAttachment("cla-origin-iid").getEidString());
                    CborParser p = CBOR.parser()
                            .cbor_parse_custom_item(
                                    () -> new HelloMessage(api.getExtensionManager().getEidFactory()),
                                    (__, ___, item) -> {
                                        for (Eid eid : item.eids) {
                                            api.getRoutingTable().addRoute(
                                                    eid,
                                                    bundle.getTagAttachment("cla-origin-iid"));
                                        }
                                    });

                    bundle.getPayloadBlock().data.observe().subscribe(
                            b -> {
                                try {
                                    while (b.hasRemaining() && !p.isDone()) {
                                        p.read(b);
                                    }
                                } catch (RxParserException rpe) {
                                    api.getLogger().i(TAG, "malformed hello message: " + rpe.getMessage());
                                }
                            });
                } else {
                    coreAPI.getLogger().i(TAG, "received hello message from: " +
                            bundle.getSource().getEidString() +
                            " but the BaseClaEid wasn't tagged - ignoring");
                }

                return Completable.complete();
            });
        } catch (RegistrarAPI.SinkAlreadyRegistered |
                RegistrarAPI.RegistrarDisabled |
                RegistrarAPI.NullArgument re) {
            api.getLogger().e(TAG, "initialization failed: " + re.getMessage());
            return;
        }

        RxBus.register(this);
    }

    /* events are serialized */
    @Subscribe
    public void onEvent(LinkLocalEntryUp up) {
        try {
            BaseClaEid eid = ((BaseClaEid) up.channel.channelEID().copy()).setPath("/hello/");
            coreAPI.getLogger().i(TAG, "sending hello message to: " + eid.getEidString());
            helloBundle.setDestination(eid);
            up.channel.sendBundle(helloBundle,
                    coreAPI.getExtensionManager().getBlockDataSerializerFactory()
            ).subscribe(
                    i -> {/* ignore */},
                    e -> coreAPI.getLogger().i(
                            TAG, "fail to send hello message: " +
                                    eid.getEidString()),
                    () -> {/* ignore */}
            );
        } catch (EidFormatException efe) {
            coreAPI.getLogger().e(TAG, "Cannot append /hello/ to IID: " + up.channel.channelEID() + " reason=" + efe.getMessage());
        }
    }

}
