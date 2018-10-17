package io.left.rightmesh.libdtn.core.agents.http;

import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.netty.handler.codec.http.HttpResponseStatus;

import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class StorageAPI {
    static Action cacheAction = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(Storage.print()));
}
