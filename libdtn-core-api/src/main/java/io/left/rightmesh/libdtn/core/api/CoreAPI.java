package io.left.rightmesh.libdtn.core.api;

/**
 * @author Lucien Loiseau on 24/10/18.
 */
public interface CoreAPI {

    RoutingAPI getRoutingEngine();

    RegistrarAPI getRegistrar();

    DeliveryAPI getDelivery();

    StorageAPI getStorage();

    ConnectionAgentAPI getConnectionAgent();

}
