package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.STCPAgent;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.routing.StaticRouting;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.storage.SimpleStorage;
import io.left.rightmesh.libdtn.storage.VolatileStorage;

/**
 * DTNCore registers all the DTN Core Component.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore {

    // ---- SINGLETON ----
    private static DTNCore instance = new DTNCore();
    public static DTNCore getInstance() {   return instance;   }
    
    private DTNCore() {
        // init all the components and load configuration
        LocalEIDTable.getInstance();
        VolatileStorage.getInstance();
        SimpleStorage.getInstance();
        LinkLocalRouting.getInstance();
        StaticRouting.getInstance();
        SmartRouting.getInstance();
        RegistrationTable.getInstance();
        STCPAgent.getInstance();
    }

    /**
     * Inject a bundle in the system. It may be a bundle received from a convergence layer
     * or a bundle received from an application agent. At this point the Bundle is assumed
     * to have been validated already.
     *
     * @param bundle to inject
     */
    public static void inject(Bundle bundle) {
    }
}
