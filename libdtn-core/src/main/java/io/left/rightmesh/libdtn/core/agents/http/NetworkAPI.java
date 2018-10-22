package io.left.rightmesh.libdtn.core.agents.http;

import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
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

    private static String dumpNetworkParameters() {
        StringBuilder sb = new StringBuilder("Routing Engine parameters:\n");
        sb.append("--------------------------\n\n");
        sb.append("forwarding: "+
                (DTNConfiguration.<Boolean>get(ENABLE_FORWARDING).value() ? "enabled" : "disabled") + "\n");
        sb.append("libdetect: "+
                (DTNConfiguration.<Boolean>get(ENABLE_COMPONENT_DETECT_PEER_ON_LAN).value() ? "enabled" : "disabled") + "\n");
        sb.append("libdetect auto-connect: "+
                (DTNConfiguration.<Boolean>get(ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT).value() ? "enabled" : "disabled") + "\n");
        sb.append("bundle auto-connect: "+
                (DTNConfiguration.<Boolean>get(ENABLE_AUTO_CONNECT_FOR_BUNDLE).value() ? "enabled" : "disabled") + "\n");
        sb.append("\n");
        return sb.toString();
    }

    private static Action dumpNetworkTables = (params, req, res) -> {
        final String linkLocal = LinkLocalRouting.print();
        final String routingTable = RoutingTable.print();
        final String netparams = dumpNetworkParameters();
        return res.setStatus(HttpResponseStatus.OK).writeString(just(linkLocal, routingTable, netparams));
    };

    private static Action dumpLinkLayerTable = (params, req, res) ->
        res.setStatus(HttpResponseStatus.OK).writeString(just(LinkLocalRouting.print()));

    private static Action dumpRoutingTable = (params, req, res) ->
            res.setStatus(HttpResponseStatus.OK).writeString(just(RoutingTable.print()));

    static Action networkAction = (params, req, res) -> using(new Router<ByteBuf, ByteBuf>()
            .GET("/network/", dumpNetworkTables)
            .GET("/network/linklayer/", dumpLinkLayerTable)
            .GET("/network/routing/", dumpRoutingTable))
            .handle(req, res);

}
