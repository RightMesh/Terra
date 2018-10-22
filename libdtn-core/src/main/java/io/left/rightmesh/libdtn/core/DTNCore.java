package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.http.APIDaemonHTTPAgent;
import io.left.rightmesh.libdtn.core.agents.APIStaticApplicationAgent;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.network.DiscoveryAgent;
import io.left.rightmesh.libdtn.core.network.CLAManager;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.left.rightmesh.libdtn.core.utils.Log;

/**
 * DTNCore registers all the DTN Core Component.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore {
    public static final String TAG = "DTNCore";

    public static void init() {
        /* init logging */
        Log.getInstance();

        /* init Routing modules */
        LinkLocalRouting.getInstance();
        RoutingTable.getInstance();
        SmartRouting.getInstance();
        AARegistrar.getInstance();

        /* init StorageAPI (index bundles in storage) */
        Storage.getInstance();

        /* init Application Agents API (receive bundle from AA) */
        APIStaticApplicationAgent.getInstance();
        APIDaemonHTTPAgent.getInstance();

        /* init connection agent */
        DiscoveryAgent.getInstance();

        /* init Convergence Layer Adapters (receive bundle from CLA) */
        CLAManager.getInstance();
        //STCPAgent.getInstance();
    }

}
