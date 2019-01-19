package io.left.rightmesh.libdtn.core;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_AA_REGISTRATION;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_LINKLOCAL_ROUTING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_MODULE_LOADER;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_ROUTING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_STORAGE;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.BundleProcessorApi;
import io.left.rightmesh.libdtn.core.api.ClaManagerApi;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.DeliveryApi;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerApi;
import io.left.rightmesh.libdtn.core.api.LinkLocalRoutingApi;
import io.left.rightmesh.libdtn.core.api.LocalEidApi;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderApi;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;
import io.left.rightmesh.libdtn.core.api.RoutingApi;
import io.left.rightmesh.libdtn.core.api.RoutingTableApi;
import io.left.rightmesh.libdtn.core.api.StorageApi;
import io.left.rightmesh.libdtn.core.events.BundleIndexed;
import io.left.rightmesh.libdtn.core.extension.ExtensionManager;
import io.left.rightmesh.libdtn.core.network.ClaManager;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEidTable;
import io.left.rightmesh.libdtn.core.routing.Registrar;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.services.NullAa;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentSpi;
import io.left.rightmesh.libdtn.core.storage.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.RxThread;
import io.left.rightmesh.librxbus.Subscribe;

/**
 * DtnCore registers all the DtnEid Core CoreComponent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DtnCore implements CoreApi {

    public static final String TAG = "DtnCore";

    private ConfigurationApi conf;
    private Log logger;
    private LocalEidApi localEidTable;
    private ExtensionManagerApi extensionManager;
    private LinkLocalRoutingApi linkLocalRouting;
    private RoutingTableApi routingTable;
    private RoutingApi routingEngine;
    private Registrar registrar;
    private StorageApi storage;
    private BundleProcessorApi bundleProcessor;
    private ClaManagerApi claManager;
    private ModuleLoaderApi moduleLoader;

    /**
     * Constructor.
     * @param conf core configuration
     */
    public DtnCore(CoreConfiguration conf) {
        this.conf = conf;

        /* core */
        this.logger = new Logger(conf);
        this.localEidTable = new LocalEidTable(this);

        /* BP block toolbox */
        this.extensionManager = new ExtensionManager(logger);

        /* routing */
        this.linkLocalRouting = new LinkLocalRouting(this);
        this.routingTable = new RoutingTable(this);
        this.routingEngine = new RoutingEngine(this);
        this.registrar = new Registrar(this);

        /* bundle processor */
        this.bundleProcessor = new BundleProcessor(this);

        /* network cla */
        this.claManager = new ClaManager(this);

        /* storage */
        this.storage = new Storage(this);

        /* runtime modules */
        this.moduleLoader = new ModuleLoader(this);
    }

    @Override
    public void init() {
        RxBus.register(this);
        linkLocalRouting.initComponent(getConf(), COMPONENT_ENABLE_LINKLOCAL_ROUTING, getLogger());
        routingTable.initComponent(getConf(), COMPONENT_ENABLE_ROUTING, getLogger());
        registrar.initComponent(getConf(), COMPONENT_ENABLE_AA_REGISTRATION, getLogger());
        storage.initComponent(getConf(), COMPONENT_ENABLE_STORAGE, getLogger());
        moduleLoader.initComponent(getConf(), COMPONENT_ENABLE_MODULE_LOADER, getLogger());

        /* starts DtnEid core services (AA) */
        ApplicationAgentSpi nullAa = new NullAa();
        nullAa.init(this.registrar, this.logger);
    }

    @Override
    public ConfigurationApi getConf() {
        return conf;
    }

    @Override
    public Log getLogger() {
        return logger;
    }

    @Override
    public LocalEidApi getLocalEid() {
        return localEidTable;
    }

    @Override
    public ExtensionManagerApi getExtensionManager() {
        return extensionManager;
    }

    @Override
    public ModuleLoaderApi getModuleLoader() {
        return moduleLoader;
    }

    @Override
    public RegistrarApi getRegistrar() {
        return registrar;
    }

    @Override
    public DeliveryApi getDelivery() {
        return registrar;
    }

    @Override
    public BundleProcessorApi getBundleProcessor() {
        return bundleProcessor;
    }

    @Override
    public StorageApi getStorage() {
        return storage;
    }

    @Override
    public ClaManagerApi getClaManager() {
        return claManager;
    }

    @Override
    public RoutingApi getRoutingEngine() {
        return routingEngine;
    }

    @Override
    public LinkLocalRoutingApi getLinkLocalRouting() {
        return linkLocalRouting;
    }

    @Override
    public RoutingTableApi getRoutingTable() {
        return routingTable;
    }

    @Subscribe(thread = RxThread.IO)
    public void onEvent(BundleIndexed event) {
        bundleProcessor.bundleDispatching(event.bundle);
    }
}
