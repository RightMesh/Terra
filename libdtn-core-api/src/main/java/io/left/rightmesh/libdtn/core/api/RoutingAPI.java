package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.reactivex.Observable;

/**
 * @author Lucien Loiseau on 24/10/18.
 */
public interface RoutingAPI {

    /**
     * Add a route to the routing table.
     *
     * @param to EID of a destination
     * @param nextHop next hop toward the destination
     */
    void addRoute(EID to, EID nextHop);

    /**
     * Returns an Observable of currently opened CLAChannel that enable a bundle to make forward
     * progress toward a destination.
     *
     * @param destination endpoint
     * @return Observable of opened CLAChannelSPI
     */
    Observable<CLAChannelSPI> findOpenedChannelTowards(EID destination);

    /**
     * Take care of this bundle for a later forwarding opportunity.
     * todo probably not an API of routing
     *
     * @param bundle to forward later
     */
    void forwardLater(final Bundle bundle);


    // todo: delete
    String printLinkLocalTable();

    // todo: delete
    String printRoutingTable();

}
