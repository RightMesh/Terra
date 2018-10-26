package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.module.core.http.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_FORWARDING;
import static io.left.rightmesh.module.core.http.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class RequestNetwork {


    private CoreAPI core;

    RequestNetwork(CoreAPI core) {
        this.core = core;
    }

    private String dumpNetworkParameters() {
        StringBuilder sb = new StringBuilder("Routing Engine parameters:\n");
        sb.append("--------------------------\n\n");
        sb.append("forwarding: "+
                (core.getConf().<Boolean>get(ENABLE_FORWARDING).value() ? "enabled" : "disabled") + "\n");
        sb.append("libdetect: "+
                (core.getConf().<Boolean>get(ENABLE_COMPONENT_DETECT_PEER_ON_LAN).value() ? "enabled" : "disabled") + "\n");
        sb.append("libdetect auto-connect: "+
                (core.getConf().<Boolean>get(ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT).value() ? "enabled" : "disabled") + "\n");
        sb.append("bundle auto-connect: "+
                (core.getConf().<Boolean>get(ENABLE_AUTO_CONNECT_FOR_BUNDLE).value() ? "enabled" : "disabled") + "\n");
        sb.append("\n");
        return sb.toString();
    }

    private Action dumpNetworkTables = (params, req, res) -> {
        final String linkLocal = core.getRoutingEngine().printLinkLocalTable();
        final String routingTable = core.getRoutingEngine().printRoutingTable();
        final String netparams = dumpNetworkParameters();
        return res.setStatus(HttpResponseStatus.OK).writeString(just(linkLocal, routingTable, netparams));
    };

    private Action dumpLinkLayerTable = (params, req, res) ->
        res.setStatus(HttpResponseStatus.OK).writeString(just(core.getRoutingEngine().printLinkLocalTable()));

    private Action dumpRoutingTable = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(core.getRoutingEngine().printRoutingTable()));

    Action networkAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/network/", dumpNetworkTables)
            .GET("/network/linklayer/", dumpLinkLayerTable)
            .GET("/network/routing/", dumpRoutingTable))
            .handle(req, res);

}
