package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {


    //Map<EventID, Set<EventCallback>>  eventmap;

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
        // step-1: read the bundle and extract the CBOR object (a map) for smart routing
        // step-2: for all key, registers the bundle to event ID
        // step-3: for each event the bundle is registered to, pull the bundle from storage,
        //         execute the corresponding bytecode routine.
    }
}
