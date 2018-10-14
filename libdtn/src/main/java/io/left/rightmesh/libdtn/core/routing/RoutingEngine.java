package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;


/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {

    public static CLAChannel findCLA(EID destination) {
        CLAChannel ret;
        if ((ret = LinkLocalRouting.findCLA(destination)) != null) {
            return ret;
        }
        if ((ret = StaticRouting.findCLA(destination)) != null) {
            return ret;
        }
        return null;
    }
}
