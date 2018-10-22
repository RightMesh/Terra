package io.left.rightmesh.libdtn.core.agents.http;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.netty.handler.codec.http.HttpResponseStatus;

import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class StorageAPI {

    private DTNCore core;

    StorageAPI(DTNCore core) {
        this.core = core;
    }

    Action cacheAction = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(core.getStorage().print()));
}
