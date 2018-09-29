package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.APIDaemonAgent;
import io.left.rightmesh.libdtn.core.agents.APIStaticAgent;
import io.left.rightmesh.libdtn.core.agents.STCPAgent;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.routing.StaticRouting;
import io.left.rightmesh.libdtn.storage.Storage;

/**
 * DTNCore registers all the DTN Core Component.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore {

    public static void init() {
        /* init local EID Configuration */
        LocalEIDTable.init();

        /* init Storage */
        Storage.init();

        /* init Routing modules */
        LinkLocalRouting.init();
        StaticRouting.init();
        SmartRouting.init();

        /* init Application Agents API */
        RegistrationTable.init();
        APIStaticAgent.init();
        APIDaemonAgent.init();

        /* init Convergence Layer Adapters */
        STCPAgent.init();
    }

}
