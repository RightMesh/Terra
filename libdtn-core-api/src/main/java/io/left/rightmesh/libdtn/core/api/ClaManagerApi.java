package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSpi;
import io.reactivex.Single;

/**
 * API for the convergence-layer adapters Manager.
 *
 * @author Lucien Loiseau on 27/11/18.
 */
public interface ClaManagerApi {

    /**
     * Add a new convergence-layer adapter (CLA).
     *
     * @param cla convergence layer adapter to add
     */
    void addCla(ConvergenceLayerSpi cla);

    /**
     * Try to create an opportunity with a certain ClaEid.
     *
     * @param eid to create an opportunity to
     * @return a single observable to the created {@link ClaChannelSpi}
     */
    Single<ClaChannelSpi> createOpportunity(ClaEid eid);

}
