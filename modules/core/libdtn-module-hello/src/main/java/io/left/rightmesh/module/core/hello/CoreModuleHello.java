package io.left.rightmesh.module.core.hello;

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
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSpi;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;

import java.nio.ByteBuffer;

/**
 * <p>CoreHelloModule is a Core Module that reacts to new peer event. For each new peer, it sends
 * an hello message containing all the local Eids of the current node. </p>
 *
 * <p>When an Hello message is received by a peer, it updates the Routing Table and adds an entry
 * matching the received peer local Eid with the ClaEid  of the ClaChannel this hello message
 * was received from.</p>
 *
 * @author Lucien Loiseau on 13/11/18.
 */
public class CoreModuleHello implements CoreModuleSpi {

    private static final String TAG = "HelloModule";
    private static final String HELLO_SINK = "/hello/";

    private CoreApi coreApi;
    private Bundle helloBundle;
    private String cookie;

    @Override
    public String getModuleName() {
        return "hello";
    }

    private void prepareHelloBundle() {
        HelloMessage hello = new HelloMessage(coreApi.getExtensionManager().getEidFactory());

        /* add node local Eid */
        hello.eids.add(coreApi.getLocalEid().localEid());

        /* add aliases */
        hello.eids.addAll(coreApi.getLocalEid().aliases());

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
    public void init(CoreApi api) {
        this.coreApi = api;

        prepareHelloBundle();

        try {
            this.cookie = api.getRegistrar().register(HELLO_SINK, (bundle) -> {
                if (bundle.getTagAttachment("cla-origin-iid") != null) {
                    coreApi.getLogger().i(TAG, "received hello message from: "
                            + bundle.getSource().getEidString()
                            + " on BaseClaEid: "
                            + bundle.<Eid>getTagAttachment("cla-origin-iid").getEidString());
                    CborParser parser = CBOR.parser()
                            .cbor_parse_custom_item(
                                    () -> new HelloMessage(
                                            api.getExtensionManager().getEidFactory()),
                                    (p, t, item) -> {
                                        for (Eid eid : item.eids) {
                                            api.getRoutingTable().addRoute(
                                                    eid,
                                                    bundle.getTagAttachment("cla-origin-iid"));
                                        }
                                    });

                    bundle.getPayloadBlock().data.observe().subscribe(
                            b -> {
                                try {
                                    while (b.hasRemaining() && !parser.isDone()) {
                                        parser.read(b);
                                    }
                                } catch (RxParserException rpe) {
                                    api.getLogger().i(TAG, "malformed hello message: "
                                            + rpe.getMessage());
                                }
                            });
                } else {
                    coreApi.getLogger().i(TAG, "received hello message from: "
                            + bundle.getSource().getEidString()
                            + " but the BaseClaEid wasn't tagged - ignoring");
                }

                return Completable.complete();
            });
        } catch (RegistrarApi.SinkAlreadyRegistered
                | RegistrarApi.RegistrarDisabled
                | RegistrarApi.NullArgument re) {
            api.getLogger().e(TAG, "initialization failed: " + re.getMessage());
            return;
        }

        RxBus.register(this);
    }

    /**
     * For every new peer that is connected, we send a hello message.
     *
     * @param up event
     */
    @Subscribe
    public void onEvent(LinkLocalEntryUp up) {
        try {
            BaseClaEid eid = ((BaseClaEid) up.channel.channelEid().copy()).setPath("/hello/");
            coreApi.getLogger().i(TAG, "sending hello message to: " + eid.getEidString());
            helloBundle.setDestination(eid);
            up.channel.sendBundle(helloBundle,
                    coreApi.getExtensionManager().getBlockDataSerializerFactory())
                    .subscribe(
                            b -> {
                                /* ignore */
                            },
                            err -> {
                                coreApi.getLogger().v(TAG, "hello failed to be sent to: "
                                        + eid.getEidString());
                            },
                            () -> {
                                coreApi.getLogger().v(TAG, "hello sent to: "
                                        + eid.getEidString());
                            });
        } catch (EidFormatException err) {
            coreApi.getLogger().e(TAG, "Could not send the hello bundle "
                    + err.getMessage());
        }
    }

}
