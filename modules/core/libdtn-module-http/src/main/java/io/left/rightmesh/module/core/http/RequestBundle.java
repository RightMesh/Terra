package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.module.core.http.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import static io.left.rightmesh.module.core.http.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 15/10/18.
 */
public class RequestBundle {

    private static final String TAG = "ApplicationAgentHTTP";
    private CoreAPI core;

    RequestBundle(CoreAPI core) {
        this.core = core;
    }

    private static class BadRequestException extends Exception {
        BadRequestException(String msg) {
            super(msg);
        }
    }

    /**
     * Fetch a bundle and deliver it to the client but don't mark the bundle as delivered
     */
    private Action aaActionGet = (params, req, res) -> {
        System.out.println("coucou");
        String param = params.get("*");
        if(param == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeStringAndFlushOnEach(just("incorrect BundleID"));
        }
        BundleID bid = BundleID.create(param);
        if (core.getStorage().contains(bid)) {
            core.getLogger().i(TAG, "delivering payload: "+bid.getBIDString());
            return Observable.<Bundle>create(s ->
                    core.getStorage().get(bid).subscribe(
                            bundle -> {
                                s.onNext(bundle);
                                s.onCompleted();
                            },
                            s::onError))
                    .flatMap((bundle) -> res.write(nettyBLOB(bundle.getPayloadBlock().data)));
        } else {
            return res.writeString(just("no such bundle"));
        }
    };


    /**
     * - UGLY -
     * unfortunately, RxNetty uses RxJava 1.x so we have to make the conversion :(
     *
     * @return Flowable of ByteBuffer
     */
    public Observable<ByteBuf> nettyBLOB(BLOB blob) {
        return Observable.create(s -> {
            blob.observe().toObservable().subscribe(
                    byteBuffer -> s.onNext(Unpooled.wrappedBuffer(byteBuffer)),
                    s::onError,
                    s::onCompleted
            );
        });
    }

    /**
     * Fetch a bundle and deliver it to the client then mark the bundle as delivered
     * (remove from storage, send report
     */
    private Action aaActionFetch = (params, req, res) -> {
        String param = params.get("*");
        BundleID bid = BundleID.create(param);
        if (core.getStorage().contains(bid)) {
            core.getLogger().i(TAG, "delivering payload: "+bid.getBIDString());
            return Observable.<Bundle>create(s ->
                    core.getStorage().get(bid).subscribe(
                            bundle -> {
                                s.onNext(bundle);
                                s.onCompleted();
                            },
                            s::onError))
                    .flatMap((bundle) -> res.write(nettyBLOB(bundle.getPayloadBlock().data)
                                    .doOnCompleted(() -> core.getBundleProcessor().bundleLocalDeliverySuccessful(bundle))));
        } else {
            return res.writeString(just("no such bundle"));
        }
    };

    /**
     * Create a new bundle and dispatch it immediatly
     */
    private Action aaActionPost = (params, req, res) -> {
        final String destEID = req.getHeader("BundleDestinationEID");
        final String reportToEID = req.getHeader("BundleReportToEID");
        final String lifetime = req.getHeader("BundleLifetime");

        try {
            final Bundle bundle = createBundleSkeletonFromHTTPHeaders(destEID, reportToEID, lifetime);
            final UntrackedByteBufferBLOB blob = new UntrackedByteBufferBLOB((int) req.getContentLength());
            return req.getContent()
                    .reduce(blob.getWritableBLOB(), (wblob, buff) -> {
                        try {
                            wblob.write(buff.nioBuffer());
                        } catch (Exception e) {
                            res.setStatus(HttpResponseStatus.BAD_REQUEST);
                        }
                        return wblob;
                    })
                    .flatMap((wblob) -> {
                        wblob.close();
                        bundle.addBlock(new PayloadBlock(blob));
                        res.setStatus(HttpResponseStatus.OK);
                        core.getBundleProcessor().bundleDispatching(bundle);
                        return res;
                    });
        } catch (BadRequestException | EIDFormatException | NumberFormatException bre) {
            core.getLogger().i(TAG, req.getDecodedPath() + " - bad request: " + bre.getMessage());
            return res.setStatus(HttpResponseStatus.BAD_REQUEST);
        }
    };

    private Bundle createBundleSkeletonFromHTTPHeaders(String destinationstr,
                                                      String reporttostr,
                                                      String lifetimestr)
            throws BadRequestException, EIDFormatException, NumberFormatException {
        EID destination;
        EID reportTo;
        long lifetime;

        if (destinationstr == null) {
            throw new BadRequestException("DestinationEID is null");
        }

        destination = new BaseEIDFactory().create(destinationstr);
        if (reporttostr == null) {
            reportTo = DTN.NullEID();
        } else {
            reportTo = new BaseEIDFactory().create(reporttostr);
        }

        if (lifetimestr == null) {
            lifetime = 0;
        } else {
            lifetime = Long.valueOf(lifetimestr);
        }

        Bundle bundle = new Bundle(destination, lifetime);
        bundle.reportto = reportTo;
        return bundle;
    }

    Action aaAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/aa/bundle/:*", aaActionGet)
            .DELETE("/aa/bundle/:*", aaActionFetch)
            .POST("/aa/bundle/", aaActionPost))
            .handle(req, res);

}
