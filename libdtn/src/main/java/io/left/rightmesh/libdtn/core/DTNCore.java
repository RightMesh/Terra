package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.http.APIDaemonHTTPAgent;
import io.left.rightmesh.libdtn.core.agents.APIStaticApplicationAgent;
import io.left.rightmesh.libdtn.core.agents.STCPAgent;
import io.left.rightmesh.libdtn.core.processor.EventProcessor;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.routing.StaticRouting;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.libdtn.utils.Log;

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

        /* init local EID ConfigurationAPI */
        LocalEIDTable.getInstance();

        /* init event processor */
        EventProcessor.getInstance();

        /* init Routing modules */
        LinkLocalRouting.getInstance();
        StaticRouting.getInstance();
        SmartRouting.getInstance();
        AARegistrar.getInstance();

        /* init StorageAPI (index bundles in storage) */
        Storage.getInstance();

        /* init Application Agents API (receive bundle from AA) */
        APIStaticApplicationAgent.getInstance();
        APIDaemonHTTPAgent.getInstance();

        /* init Convergence Layer Adapters (receive bundle from CLA) */
        STCPAgent.getInstance();
    }

}
