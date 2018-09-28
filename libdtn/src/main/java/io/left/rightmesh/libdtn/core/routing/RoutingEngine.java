package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {

    public static CLAChannel findCLA(Bundle bundle) {
        CLAChannel ret;
        if((ret = LinkLocalRouting.findCLA(bundle)) != null) {
            return ret;
        }
        if((ret = StaticRouting.findCLA(bundle)) != null) {
            return ret;
        }
        return null;
    }

    public static void forwardLater(Bundle bundle) {
    }
}
