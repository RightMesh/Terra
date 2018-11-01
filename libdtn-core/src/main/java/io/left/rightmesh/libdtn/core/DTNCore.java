package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.api.BundleProcessorAPI;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.LocalEIDAPI;
import io.left.rightmesh.libdtn.core.events.BundleIndexed;
import io.left.rightmesh.libdtn.core.network.ConnectionAgent;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.routing.Registrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.network.DiscoveryAgent;
import io.left.rightmesh.libdtn.core.network.CLAManager;
import io.left.rightmesh.libdtn.core.storage.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.left.rightmesh.libdtn.core.api.ConnectionAgentAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.DeliveryAPI;
import io.left.rightmesh.libdtn.core.api.RoutingAPI;
import io.left.rightmesh.libdtn.core.api.StorageAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.RxThread;
import io.left.rightmesh.librxbus.Subscribe;

/**
 * DTNCore registers all the DTN Core BaseComponent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore implements CoreAPI {

    public static final String TAG = "DTNCore";

    private ConfigurationAPI conf;
    private Logger logger;
    private LocalEIDAPI localEIDTable;
    private LinkLocalRouting linkLocalRouting;
    private RoutingTable routingTable;
    private RoutingAPI routingEngine;
    private Registrar registrar;
    private StorageAPI storage;
    private BundleProcessorAPI bundleProcessor;
    private ConnectionAgentAPI connectionAgent;
    private DiscoveryAgent discoveryAgent;
    private CLAManager claManager;
    private ModuleLoader moduleLoader;

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
        core.bundleProcessor = new BundleProcessor(core);

        /* network */
        core.connectionAgent = new ConnectionAgent(core);
        core.discoveryAgent = new DiscoveryAgent(core);
        core.claManager = new CLAManager(core);

        /* storage */
        RxBus.register(core);
        core.storage = new Storage(core.conf, core.logger);


        /* modules */
        core.moduleLoader = new ModuleLoader(core);

        return core;
    }

    @Override
    public ConfigurationAPI getConf() {
        return conf;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public LocalEIDAPI getLocalEID() {
        return localEIDTable;
    }

    public LinkLocalRouting getLinkLocalRouting() {
        return linkLocalRouting;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    @Override
    public RoutingAPI getRoutingEngine() {
        return routingEngine;
    }

    @Override
    public RegistrarAPI getRegistrar() {
        return registrar;
    }

    @Override
    public DeliveryAPI getDelivery() {
        return registrar;
    }

    @Override
    public BundleProcessorAPI getBundleProcessor() {
        return bundleProcessor;
    }

    @Override
    public StorageAPI getStorage() {
        return storage;
    }

    @Override
    public ConnectionAgentAPI getConnectionAgent() {
        return connectionAgent;
    }

    public DiscoveryAgent getDiscoveryAgent() {
        return discoveryAgent;
    }

    public CLAManager getClaManager() {
        return claManager;
    }


    @Subscribe( thread = RxThread.IO )
    public void onEvent(BundleIndexed event) {
        bundleProcessor.bundleDispatching(event.bundle);
    }
}
