package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;
import java.util.Map;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_ROUTING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.STATIC_ROUTE_CONFIGURATION;

/**
 * Static Routing is a routing component that uses the static route table to take
 * forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class StaticRouting extends Component {

    // ---- SINGLETON ----
    private static StaticRouting instance = new StaticRouting();
    public static StaticRouting getInstance() { return instance; }
    public static void init() {}

    private Map<EID, EID> staticRoutingTable = new HashMap<>();

    private StaticRouting() {
        super(COMPONENT_ENABLE_STATIC_ROUTING);
        DTNConfiguration.<Map<EID, EID>>get(STATIC_ROUTE_CONFIGURATION).observe().subscribe(
                m -> staticRoutingTable = m);
    }

    static CLAChannel findCLA(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return null;
        }

        for (EID entry : getInstance().staticRoutingTable.keySet()) {
            if (bundle.destination.matches(entry)) {
                EID next = getInstance().staticRoutingTable.get(entry);
                CLAChannel channel = LinkLocalRouting.isPeerLinkLocal(next);
                if(channel != null) {
                    return channel;
                }
            }
        }

        return null;
    }
}
