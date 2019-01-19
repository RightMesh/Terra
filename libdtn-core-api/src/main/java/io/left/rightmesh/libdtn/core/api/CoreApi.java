package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * API for the core. It basically acts as a hub to access all of the subcomponents.
 *
 * @author Lucien Loiseau on 24/10/18.
 */
public interface CoreApi {

    /**
     * init the core.
     */
    void init();

    ConfigurationApi getConf();

    Log getLogger();

    LocalEidApi getLocalEid();

    ExtensionManagerApi getExtensionManager();

    RoutingApi getRoutingEngine();

    RegistrarApi getRegistrar();

    DeliveryApi getDelivery();

    BundleProcessorApi getBundleProcessor();

    StorageApi getStorage();

    ClaManagerApi getClaManager();

    LinkLocalRoutingApi getLinkLocalRouting();

    RoutingTableApi getRoutingTable();

    ModuleLoaderApi getModuleLoader();

}
