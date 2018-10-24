package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.core.api.cla.CLAChannelSPI;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 24/10/18.
 */
public interface ConnectionAgentAPI {
    /**
     * Try to create an opportunity for this host that was detected with libdetect.
     *
     * @param host to connect to
     * @return an open channel to this host upon success, or an error upon failure
     */
    Single<CLAChannelSPI> createOpportunityLibDetect(String host);

    /**
     * Try to create an opportunity for this CLA eid that is one of the route toward
     * a bundle destination EID
     *
     * @param CLA-EID to connect to
     * @return an open channel to this CLAChannel upon success, or an error upon failure
     */
    Single<CLAChannelSPI> createOpportunityForBundle(CLA eid);
}
