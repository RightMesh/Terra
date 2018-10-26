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
     * @param bundle
     */
    void forwardLater(final Bundle bundle);


    // todo: delete
    String printLinkLocalTable();

    // todo: delete
    String printRoutingTable();

}
