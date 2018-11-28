package io.left.rightmesh.libdtn.core.spi.cla;

import io.left.rightmesh.libdtn.common.data.eid.CLAEID;
import io.left.rightmesh.libdtn.common.data.eid.CLAEIDParser;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.spi.ModuleSPI;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A ConvergenceLayerSPI is an abstraction of the underlying protocol used as a BaseCLAEID.
 *
 * @author Lucien Loiseau on 16/10/18.
 */
public interface ConvergenceLayerSPI  extends ModuleSPI {

    /**
     * get the CLA-EID parser for this convergence layer. The CL name for which
     * this module is relevant MUST be the module name.
     * @return CLAEIDParser
     */
    CLAEIDParser getCLAEIDParser();

    /**
     * When a BaseCLAEID is started it should return an Observable of CLAChannelSPI used to actually send
     * and receive bundles.
     *
     * @param api configuration
     * @param logger logger instance
     * @return Flowable of Bundle
     */
    Observable<CLAChannelSPI> start(ConfigurationAPI api, Log logger);

    /**
     * When a BaseCLAEID is stopped, it should stop creating any new CLAChannelSPI and terminate the
     * observable. It is an implementation specific decision wether or not to close all the
     * underlying CLAChannels that were previously openned.
     */
    void stop();

    /**
     * Tries to open a channel to the given BaseCLAEID-specific EID.
     *
     * @param eid of the peer to open a channel too, must be BaseCLAEID-specific
     * @return Single of CLAChannelSPI if successful, error otherwise
     */
    Single<CLAChannelSPI> open(CLAEID eid);

}
