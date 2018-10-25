package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.daemon.http.APIDaemonHTTPAgent;
import io.left.rightmesh.libdtn.core.network.ConnectionAgent;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.routing.Registrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.network.DiscoveryAgent;
import io.left.rightmesh.libdtn.core.network.CLAManager;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.left.rightmesh.libdtn.core.api.ConnectionAgentAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.DeliveryAPI;
import io.left.rightmesh.libdtn.core.api.RoutingAPI;
import io.left.rightmesh.libdtn.core.api.StorageAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;

/**
 * DTNCore registers all the DTN Core BaseComponent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore implements CoreAPI {

    public static final String TAG = "DTNCore";

    private DTNConfiguration conf;
    private Logger logger;
    private LocalEIDTable localEIDTable;
    private LinkLocalRouting linkLocalRouting;
    private RoutingTable routingTable;
    private RoutingAPI routingEngine;
    private Registrar registrar;
    private StorageAPI storage;
    private BundleProcessor bundleProcessor;
    private ConnectionAgentAPI connectionAgent;
    private APIDaemonHTTPAgent httpApi;
    private DiscoveryAgent discoveryAgent;
    private CLAManager claManager;

    public static CoreAPI init(DTNConfiguration conf) {
        DTNCore core = new DTNCore();
        core.conf = conf;

        /* core */
        core.logger = new Logger(conf);
        core.localEIDTable = new LocalEIDTable(core);

        /* routing */
        core.linkLocalRouting = new LinkLocalRouting(core);
        core.routingTable = new RoutingTable(core);
        core.routingEngine = new RoutingEngine(core);
        core.registrar = new Registrar(core);

        /*  core */
        core.storage = new Storage(core.conf, core.logger);
        core.bundleProcessor = new BundleProcessor(core);

        /* network */
        core.connectionAgent = new ConnectionAgent(core);
        core.discoveryAgent = new DiscoveryAgent(core);
        core.claManager = new CLAManager(core);

        /* api */
        core.httpApi = new APIDaemonHTTPAgent(core);

        return core;
    }

    public DTNConfiguration getConf() {
        return conf;
    }

    public Logger getLogger() {
        return logger;
    }

    public LocalEIDTable getLocalEIDTable() {
        return localEIDTable;
    }

    public LinkLocalRouting getLinkLocalRouting() {
        return linkLocalRouting;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public RoutingAPI getRoutingEngine() {
        return routingEngine;
    }

    public RegistrarAPI getRegistrar() {
        return registrar;
    }

    public DeliveryAPI getDelivery() {
        return registrar;
    }

    public StorageAPI getStorage() {
        return storage;
    }

    public ConnectionAgentAPI getConnectionAgent() {
        return connectionAgent;
    }

    public DiscoveryAgent getDiscoveryAgent() {
        return discoveryAgent;
    }

    public BundleProcessor getBundleProcessor() {
        return bundleProcessor;
    }

    public CLAManager getClaManager() {
        return claManager;
    }
}