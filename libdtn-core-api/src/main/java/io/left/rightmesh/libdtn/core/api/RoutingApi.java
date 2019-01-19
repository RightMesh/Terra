package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.reactivex.Observable;

/**
 * API for the routing engine.
 *
 * @author Lucien Loiseau on 24/10/18.
 */
public interface RoutingApi {

    /**
     * Returns an Observable of currently opened CLAChannel that enable a bundle to make forward
     * progress toward a destination.
     *
     * @param destination endpoint
     * @return Observable of opened ClaChannelSpi
     */
    Observable<ClaChannelSpi> findOpenedChannelTowards(Eid destination);

    /**
     * Take care of this bundle for a later forwarding opportunity.
     *
     * @param bundle to forward later
     */
    void forwardLater(final Bundle bundle);


    // todo: delete
    String printLinkLocalTable();

    // todo: delete
    String printRoutingTable();

}
