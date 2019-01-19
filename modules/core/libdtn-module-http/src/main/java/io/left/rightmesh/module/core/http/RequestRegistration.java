package io.left.rightmesh.module.core.http;

import java.nio.charset.Charset;

import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.left.rightmesh.module.core.http.nettyrouter.Router;

import static io.left.rightmesh.module.core.http.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class RequestRegistration {

    private CoreApi core;

    RequestRegistration(CoreApi core) {
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
                    try {
                        if (core.getRegistrar().isRegistered(sink)) {
                            return res.setStatus(HttpResponseStatus.CONFLICT)
                                    .writeString(just("sink is already registered: " + sink))
                                    .writeString(just(core.getRegistrar().printTable()));

                        } else {
                            String cookie = core.getRegistrar().register(sink);
                            return res.setStatus(HttpResponseStatus.OK)
                                    .writeString(just("sink registered: " + sink))
                                    .writeString(just("registration cookie: " + cookie))
                                    .writeString(just(core.getRegistrar().printTable()));
                        }
                    } catch(RegistrarApi.RegistrarException re) {
                        return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                                .writeString(just("sink is not valid"));
                    }
                });

    private Action registerActionDelete = (params, req, res) ->
        req.getContent()
                .reduce("", (content, buff) ->
                        content+buff.toString(Charset.defaultCharset()))
                .flatMap((sink) -> {
                    try {
                        core.getRegistrar().unregister(sink, sink);
                        return res.setStatus(HttpResponseStatus.OK)
                                .writeString(just("sink unregistered: " + sink))
                                .writeString(just(core.getRegistrar().printTable()));
                    } catch(RegistrarApi.RegistrarException re) {
                        return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                                .writeString(just("request not valid"));
                    }
                });

    Action registerAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/registration/", registerActionGet)
            .POST("/registration/", registerActionAdd)
            .DELETE("/registration/", registerActionDelete))
            .handle(req, res);
}
