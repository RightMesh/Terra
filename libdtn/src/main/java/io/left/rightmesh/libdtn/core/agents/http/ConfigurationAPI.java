package io.left.rightmesh.libdtn.core.agents.http;

import java.util.Set;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.utils.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import static io.left.rightmesh.libdtn.utils.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class ConfigurationAPI {

    private static Action confLocalEID = (params, req, res) -> {
        final String localeid = LocalEIDTable.localEID().toString();
        return res.setStatus(HttpResponseStatus.OK).writeString(just("localeid=" + localeid));
    };

    private static Action confAliases = (params, req, res) -> {
        final Set<EID> aliases = DTNConfiguration.<Set<EID>>get(DTNConfiguration.Entry.ALIASES).value();

        return res.setStatus(HttpResponseStatus.OK).writeString(
                Observable.from(aliases)
                        .flatMap((a) -> just(a.toString() + "\n")));
    };

    private static Action confComponents = (params, req, res) -> res.setStatus(HttpResponseStatus.OK).writeString(
                Observable.from(Component.getAllComponents()).flatMap((c) ->
                        just(c.getComponentName() + " - " + (c.isEnabled() ? "UP" : "DOWN") + "\n")));

    private static Action dumpConfiguration = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just("conf"));

    static Action confAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
                .GET("/conf/", dumpConfiguration)
                .GET("/conf/localeid/", confLocalEID)
                .GET("/conf/aliases/", confAliases)
                .GET("/conf/components/", confComponents))
                .handle(req, res);

}
