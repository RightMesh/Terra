package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.blob.Blob;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBlob;
import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
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
     * Fetch a bundle and deliver it to the api but don't mark the bundle as delivered
     */
    private Action aaActionGet = (params, req, res) -> {
        System.out.println("coucou");
        String param = params.get("*");
        if(param == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeStringAndFlushOnEach(just("incorrect BundleId"));
        }
        BundleId bid = BundleId.create(param);
        if (core.getStorage().contains(bid)) {
            core.getLogger().i(TAG, "delivering payload: "+bid.getBidString());
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
    public Observable<ByteBuf> nettyBLOB(Blob blob) {
        return Observable.create(s -> {
            blob.observe().toObservable().subscribe(
                    byteBuffer -> s.onNext(Unpooled.wrappedBuffer(byteBuffer)),
                    s::onError,
                    s::onCompleted
            );
        });
    }

    /**
     * Fetch a bundle and deliver it to the api then mark the bundle as delivered
     * (remove from storage, send report
     */
    private Action aaActionFetch = (params, req, res) -> {
        String param = params.get("*");
        BundleId bid = BundleId.create(param);
        if (core.getStorage().contains(bid)) {
            core.getLogger().i(TAG, "delivering payload: "+bid.getBidString());
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
            final UntrackedByteBufferBlob blob = new UntrackedByteBufferBlob((int) req.getContentLength());
            return req.getContent()
                    .reduce(blob.getWritableBlob(), (wblob, buff) -> {
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
        } catch (BadRequestException | EidFormatException | NumberFormatException bre) {
            core.getLogger().i(TAG, req.getDecodedPath() + " - bad request: " + bre.getMessage());
            return res.setStatus(HttpResponseStatus.BAD_REQUEST);
        }
    };

    private Bundle createBundleSkeletonFromHTTPHeaders(String destinationstr,
                                                      String reporttostr,
                                                      String lifetimestr)
            throws BadRequestException, EidFormatException, NumberFormatException {
        Eid destination;
        Eid reportTo;
        long lifetime;

        if (destinationstr == null) {
            throw new BadRequestException("DestinationEID is null");
        }

        destination = new BaseEidFactory().create(destinationstr);
        if (reporttostr == null) {
            reportTo = DtnEid.nullEid();
        } else {
            reportTo = new BaseEidFactory().create(reporttostr);
        }

        if (lifetimestr == null) {
            lifetime = 0;
        } else {
            lifetime = Long.valueOf(lifetimestr);
        }

        Bundle bundle = new Bundle(destination, lifetime);
        bundle.setReportto(reportTo);
        return bundle;
    }

    Action aaAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/aa/bundle/:*", aaActionGet)
            .DELETE("/aa/bundle/:*", aaActionFetch)
            .POST("/aa/bundle/", aaActionPost))
            .handle(req, res);

}
