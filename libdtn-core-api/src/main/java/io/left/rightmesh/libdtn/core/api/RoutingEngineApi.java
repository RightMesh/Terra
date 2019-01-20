package io.left.rightmesh.libdtn.core.api;

/**
 * DeliveryApi is the routing strategies contract. It provides access to all the routine strategies
 * submodule.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public interface RoutingEngineApi extends RoutingStrategyApi {

    class RoutingStrategyAlreadyRegistered extends Exception {
    }

    /**
     * Register a new routing strategy.
     *
     * @param routingStrategy to add
     * @throws RoutingStrategyAlreadyRegistered if the routing strategy Id was already registered
     */
    void addRoutingStrategy(RoutingStrategyApi routingStrategy)
            throws RoutingStrategyAlreadyRegistered;
}
