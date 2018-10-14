package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.APIDaemonCBORAgent;
import io.left.rightmesh.libdtn.core.agents.http.APIDaemonHTTPAgent;
import io.left.rightmesh.libdtn.core.agents.APIStaticApplicationAgent;
import io.left.rightmesh.libdtn.core.agents.STCPAgent;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
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

        /* init StorageAPI */
        Storage.getInstance();

        /* init Routing modules */
        LinkLocalRouting.getInstance();
        StaticRouting.getInstance();
        SmartRouting.getInstance();

        /* init Application Agents API */
        RegistrationTable.getInstance();
        APIStaticApplicationAgent.getInstance();
        APIDaemonCBORAgent.getInstance();
        APIDaemonHTTPAgent.getInstance();

        /* init Convergence Layer Adapters */
        STCPAgent.getInstance();
    }

}
