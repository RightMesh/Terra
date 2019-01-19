package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.netty.handler.codec.http.HttpResponseStatus;

import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class RequestStorage {

    private CoreApi core;

    RequestStorage(CoreApi core) {
        this.core = core;
    }

    Action cacheAction = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(core.getStorage().print()));
}
