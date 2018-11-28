package io.left.rightmesh.libdtn.core;

import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.BlockManagerAPI;
import io.left.rightmesh.libdtn.core.api.BundleProcessorAPI;
import io.left.rightmesh.libdtn.core.api.CLAManagerAPI;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.LinkLocalRoutingAPI;
import io.left.rightmesh.libdtn.core.api.LocalEIDAPI;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderAPI;
import io.left.rightmesh.libdtn.core.api.RoutingTableAPI;
import io.left.rightmesh.libdtn.core.blocks.BlockManager;
import io.left.rightmesh.libdtn.core.events.BundleIndexed;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.routing.Registrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.network.CLAManager;
import io.left.rightmesh.libdtn.core.services.NullAA;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentSPI;
import io.left.rightmesh.libdtn.core.storage.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;
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
    private Log logger;
    private LocalEIDAPI localEIDTable;
    private BlockManagerAPI blockManager;
    private LinkLocalRoutingAPI linkLocalRouting;
    private RoutingTableAPI routingTable;
    private RoutingAPI routingEngine;
    private Registrar registrar;
    private StorageAPI storage;
    private BundleProcessorAPI bundleProcessor;
    private CLAManagerAPI claManager;
    private ModuleLoaderAPI moduleLoader;
    private List<ApplicationAgentSPI> coreServices;

    public DTNCore(DTNConfiguration conf) {
        this.conf = conf;

        /* core */
        this.logger = new Logger(conf);
        this.localEIDTable = new LocalEIDTable(this);

        /* BP block toolbox */
        this.blockManager = new BlockManager();

        /* routing */
        this.linkLocalRouting = new LinkLocalRouting(this);
        this.routingTable = new RoutingTable(this);
        this.routingEngine = new RoutingEngine(this);
        this.registrar = new Registrar(this);

        /* bundle processor */
        this.bundleProcessor = new BundleProcessor(this);

        /* network cla */
        this.claManager = new CLAManager(this);

        /* storage */
        RxBus.register(this);
        this.storage = new Storage(this);

        /* runtime modules */
        this.moduleLoader = new ModuleLoader(this);

        /* core services (AA) */
        this.coreServices = new LinkedList<>();
        this.coreServices.add(new NullAA());
        for(ApplicationAgentSPI aa : this.coreServices) {
            aa.init(this.registrar, this.logger);
        }
    }

    @Override
    public ConfigurationAPI getConf() {
        return conf;
    }

    @Override
    public Log getLogger() {
        return logger;
    }

    @Override
    public LocalEIDAPI getLocalEID() {
        return localEIDTable;
    }

    @Override
    public BlockManagerAPI getBlockManager() {
        return blockManager;
    }

    @Override
    public ModuleLoaderAPI getModuleLoader() {
        return moduleLoader;
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
    public CLAManagerAPI getClaManager() {
        return claManager;
    }

    @Override
    public RoutingAPI getRoutingEngine() {
        return routingEngine;
    }

    @Override
    public LinkLocalRoutingAPI getLinkLocalRouting() {
        return linkLocalRouting;
    }

    @Override
    public RoutingTableAPI getRoutingTable() {
        return routingTable;
    }

    @Subscribe( thread = RxThread.IO )
    public void onEvent(BundleIndexed event) {
        bundleProcessor.bundleDispatching(event.bundle);
    }
}
