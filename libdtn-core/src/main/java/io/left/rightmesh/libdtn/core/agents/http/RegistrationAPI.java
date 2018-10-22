package io.left.rightmesh.libdtn.core.agents.http;

import java.nio.charset.Charset;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.left.rightmesh.libdtn.core.utils.nettyrouter.Router;

import static io.left.rightmesh.libdtn.core.utils.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class RegistrationAPI {

    private DTNCore core;

    RegistrationAPI(DTNCore core) {
        this.core = core;
    }

    private Action registerActionGet = (params, req, res) ->
            res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just(core.getRegistrar().printTable()));

    private Action registerActionAdd = (params, req, res) ->
            req.getContent()
                .reduce("", (content, buff) ->
                        content+buff.toString(Charset.defaultCharset()))
                .flatMap((sink) -> {
                    if(core.getRegistrar().isRegistered(sink)) {
                        return res.setStatus(HttpResponseStatus.CONFLICT)
                                .writeString(just("sink is already registered: " + sink))
                                .writeString(just(core.getRegistrar().printTable()));

                    }else if(core.getRegistrar().register(sink)) {
                        return res.setStatus(HttpResponseStatus.OK)
                                .writeString(just("sink registered: " + sink))
                                .writeString(just(core.getRegistrar().printTable()));
                    } else {
                        return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                                .writeString(just("sink is not valid"));
                    }
                });

    private Action registerActionDelete = (params, req, res) ->
        req.getContent()
                .reduce("", (content, buff) ->
                        content+buff.toString(Charset.defaultCharset()))
                .flatMap((sink) -> {
                    if(core.getRegistrar().unregister(sink)) {
                        return res.setStatus(HttpResponseStatus.OK)
                                .writeString(just("sink unregistered: " + sink))
                                .writeString(just(core.getRegistrar().printTable()));
                    } else {
                        return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                                .writeString(just("no such sink registered"))
                                .writeString(just(core.getRegistrar().printTable()));
                    }
                });

    Action registerAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/registration/", registerActionGet)
            .POST("/registration/", registerActionAdd)
            .DELETE("/registration/", registerActionDelete))
            .handle(req, res);
}
