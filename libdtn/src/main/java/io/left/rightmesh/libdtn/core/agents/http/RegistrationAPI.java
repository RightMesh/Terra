package io.left.rightmesh.libdtn.core.agents.http;

import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.netty.handler.codec.http.HttpResponseStatus;

import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class RegistrationAPI {

    static Action registerAction = (params, req, res) -> {
        String sink = params.get("*");
        if (sink == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("incorrect sink"))
                    .writeString(just(AARegistrar.printTable()));
        }
        if (AARegistrar.register(sink)) {
            return res.setStatus(HttpResponseStatus.OK)
                    .writeString(just("sink registered: " + sink))
                    .writeString(just(AARegistrar.printTable()));
        } else {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("sink already registered: " + sink))
                    .writeString(just(AARegistrar.printTable()));
        }
    };

    static Action unregisterAction = (params, req, res) -> {
        String sink = params.get("*");
        if (sink == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("incorrect sink"))
                    .writeString(just(AARegistrar.printTable()));
        }
        if (AARegistrar.unregister(sink)) {
            return res.setStatus(HttpResponseStatus.OK)
                    .writeString(just("sink unregistered: " + sink))
                    .writeString(just(AARegistrar.printTable()));
        } else {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("no such sink: " + sink))
                    .writeString(just(AARegistrar.printTable()));
        }
    };
}
