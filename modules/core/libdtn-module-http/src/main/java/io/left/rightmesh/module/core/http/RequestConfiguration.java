package io.left.rightmesh.module.core.http;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.module.core.http.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import static io.left.rightmesh.module.core.http.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class RequestConfiguration {

    private CoreAPI core;

    RequestConfiguration(CoreAPI core) {
        this.core = core;
    }

    private Action confLocalEID = (params, req, res) -> {
        final String localeid = core.getLocalEID().localEID().getEidString();
        return res.setStatus(HttpResponseStatus.OK).writeString(just("localeid=" + localeid));
    };

    private Action confAliases = (params, req, res) -> {
        final Set<Eid> aliases = core.getLocalEID().aliases();

        return res.setStatus(HttpResponseStatus.OK).writeString(
                Observable.from(aliases)
                        .flatMap((a) -> just(a.getEidString() + "\n")));
    };

    private Action dumpConfiguration = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just("conf"));

    Action confAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
                .GET("/conf/", dumpConfiguration)
                .GET("/conf/localeid/", confLocalEID)
                .GET("/conf/aliases/", confAliases))
                .handle(req, res);

}
