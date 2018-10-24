package io.left.rightmesh.libdtn.core.http;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.utils.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.ENABLE_AUTO_CONNECT_FOR_BUNDLE;
import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT;
import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN;
import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.core.utils.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class NetworkAPI {


    private DTNCore core;

    NetworkAPI(DTNCore core) {
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
        final String linkLocal = core.getLinkLocalRouting().print();
        final String routingTable = core.getRoutingTable().print();
        final String netparams = dumpNetworkParameters();
        return res.setStatus(HttpResponseStatus.OK).writeString(just(linkLocal, routingTable, netparams));
    };

    private Action dumpLinkLayerTable = (params, req, res) ->
        res.setStatus(HttpResponseStatus.OK).writeString(just(core.getLinkLocalRouting().print()));

    private Action dumpRoutingTable = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(core.getRoutingTable().print()));

    Action networkAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/network/", dumpNetworkTables)
            .GET("/network/linklayer/", dumpLinkLayerTable)
            .GET("/network/routing/", dumpRoutingTable))
            .handle(req, res);

}
