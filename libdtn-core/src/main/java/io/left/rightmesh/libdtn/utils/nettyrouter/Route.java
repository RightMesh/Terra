package io.left.rightmesh.libdtn.utils.nettyrouter;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * creates a Handler that comes with matched URL params
 */
@FunctionalInterface
public interface Route<I,O> extends RequestHandler<I, O> {

    default Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        return handle(new HashMap<>(), request, response);
    }

    Observable<Void> handle(Map<String, String> params, HttpServerRequest<I> request, HttpServerResponse<O> response);
}
