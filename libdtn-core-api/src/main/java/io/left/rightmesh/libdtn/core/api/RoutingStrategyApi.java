package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.reactivex.Single;

/**
 * A Routing Strategy provides method to process and decide how to route a Bundle.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public interface RoutingStrategyApi {

    enum RoutingStrategyResult {
        Forwarded,
        CustodyAccepted,
        CustodyRefused
    }

    /**
     * returns the Id identifying this strategy.
     *
     * @return id
     */
    int getRoutingStrategyId();

    /**
     * Route a Bundle by executing the routing strategy.
     *
     * @param bundle to route
     * @return a RoutingStrategyResult
     */
    Single<RoutingStrategyResult> route(Bundle bundle);

}
