package io.left.rightmesh.libdtn.core.spi.cla;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.spi.ModuleSPI;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A ConvergenceLayerSPI is an abstraction of the underlying protocol used as a CLA.
 *
 * @author Lucien Loiseau on 16/10/18.
 */
public interface ConvergenceLayerSPI  extends ModuleSPI {

    /**
     * Set the logger
     */
    void setLogger(Log logger);


    /**
     * When a CLA is started it should return an Observable of CLAChannelSPI used to actually send
     * and receive bundles.
     *
     * @return Flowable of Bundle
     */
    Observable<CLAChannelSPI> start(ConfigurationAPI api);

    /**
     * When a CLA is stopped, it should stop creating any new CLAChannelSPI and terminate the
     * observable. It is an implementation specific decision wether or not to close all the
     * underlying CLAChannels that were previously openned.
     */
    void stop();

    /**
     * Tries to open a channel to the given CLA-specific EID.
     *
     * @param eid of the peer to open a channel too, must be CLA-specific
     * @return Single of CLAChannelSPI if successful, error otherwise
     */
    Single<CLAChannelSPI> open(CLA eid);

}
