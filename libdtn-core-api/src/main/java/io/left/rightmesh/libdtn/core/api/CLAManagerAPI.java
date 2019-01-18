package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 27/11/18.
 */
public interface CLAManagerAPI {

    /**
     * Add a new ConvergenceLayerAdapter.
     *
     * @param cla convergence layer adapter to add
     */
    void addCLA(ConvergenceLayerSPI cla);

    /**
     * Try to create an opportunity for a certain ClaEid.
     *
     * @param eid to create an opportunity to
     */
    Single<CLAChannelSPI> createOpportunity(ClaEid eid);

}
