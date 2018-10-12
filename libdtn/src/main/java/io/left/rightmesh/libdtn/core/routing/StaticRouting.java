package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;
import java.util.Map;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.utils.Log;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_ROUTING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.STATIC_ROUTE_CONFIGURATION;

/**
 * Static Routing is a routing component that uses the static route table to take
 * forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class StaticRouting extends Component {

    private static final String TAG = "StaticRouting";

    // ---- SINGLETON ----
    private static StaticRouting instance = new StaticRouting();
    public static StaticRouting getInstance() { return instance; }
    public static void init() {
        instance.initComponent(COMPONENT_ENABLE_STATIC_ROUTING);
        DTNConfiguration.<Map<EID, EID>>get(STATIC_ROUTE_CONFIGURATION).observe().subscribe(
                m -> instance.staticRoutingTable = m);
    }

    private Map<EID, EID> staticRoutingTable = new HashMap<>();

    @Override
    protected String getComponentName() {
        return TAG;
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
