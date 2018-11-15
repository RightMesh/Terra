package io.left.rightmesh.module.aa.hello;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;

/**
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

    private void initHelloBundle() {
        HelloMessage hello = new HelloMessage();

        /* add node main EID */
        hello.eids.add(coreAPI.getLocalEID().localEID());

        /* add aliases */
        hello.eids.addAll(coreAPI.getLocalEID().aliases());

        /* get size of hello message for the payload */
        long size = hello.encode().observe()
                .map(ByteBuffer::remaining)
                .reduce(0, (a, b) -> a + b)
                .blockingGet();

        /* serialize the hello message into a BLOB (for payload) */
        UntrackedByteBufferBLOB blobHello = new UntrackedByteBufferBLOB((int) size);
        final WritableBLOB wblob = blobHello.getWritableBLOB();
        hello.encode().observe()
                .map(wblob::write)
                .doOnComplete(wblob::close)
                .subscribe();

        /* create Hello Bundle */
        helloBundle = new Bundle(DTN.NullEID());
        helloBundle.addBlock(new PayloadBlock(blobHello));
    }

    @Override
    public void init(CoreAPI api) {
        this.coreAPI = api;

        initHelloBundle();

        try {
            api.getRegistrar().register("/hello/", (bundle) -> {
                if(bundle.getTagAttachment("cla-origin-iid") != null) {
                    CborParser p = CBOR.parser()
                            .cbor_parse_custom_item(
                                    HelloMessage::new,
                                    (__, ___, item) -> {
                                        for (EID eid : item.eids) {
                                            coreAPI.getRoutingEngine().addRoute(
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
            CLA eid = up.channel.channelEID().setPath("/hello/");
            coreAPI.getLogger().i(TAG, "sending hello message to: " + eid.getEIDString());
            helloBundle.destination = eid;
            up.channel.sendBundle(helloBundle).ignoreElements().subscribe();
        } catch(EID.EIDFormatException efe) {
            coreAPI.getLogger().e(TAG, "Cannot append /hello/ to IID: "+up.channel.channelEID()+" reason="+efe.getMessage());
        }
    }

}
