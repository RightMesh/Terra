package io.left.rightmesh.libdtn.core.http;

import java.util.Set;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.utils.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import static io.left.rightmesh.libdtn.core.utils.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class ConfigurationAPI {

    private DTNCore core;

    ConfigurationAPI(DTNCore core) {
        this.core = core;
    }

    private Action confLocalEID = (params, req, res) -> {
        final String localeid = core.getLocalEIDTable().localEID().getEIDString();
        return res.setStatus(HttpResponseStatus.OK).writeString(just("localeid=" + localeid));
    };

    private Action confAliases = (params, req, res) -> {
        final Set<EID> aliases = core.getConf().<Set<EID>>get(DTNConfiguration.Entry.ALIASES).value();

        return res.setStatus(HttpResponseStatus.OK).writeString(
                Observable.from(aliases)
                        .flatMap((a) -> just(a.getEIDString() + "\n")));
    };

    private Action confComponents = (params, req, res) -> res.setStatus(HttpResponseStatus.OK).writeString(
                Observable.from(BaseComponent.getAllComponents()).flatMap((c) ->
                        just(c.getComponentName() + " - " + (c.isEnabled() ? "UP" : "DOWN") + "\n")));

    private Action dumpConfiguration = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just("conf"));

    Action confAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
                .GET("/conf/", dumpConfiguration)
                .GET("/conf/localeid/", confLocalEID)
                .GET("/conf/aliases/", confAliases)
                .GET("/conf/components/", confComponents))
                .handle(req, res);

}
