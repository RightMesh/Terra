package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.reactivex.Single;

/**
 * Api for the default direct routing strategy.
 *
 * @author Lucien Loiseau on 20/01/19.
 */
public interface DirectRoutingStrategyApi extends RoutingStrategyApi {

    /**
     * routeLater will monitor the direct neighborhood and forward the bundle if there is
     * an opportunity for direct forwarding.
     *
     * @param bundle to route
     */
    Single<RoutingStrategyResult> routeLater(final Bundle bundle);

}
