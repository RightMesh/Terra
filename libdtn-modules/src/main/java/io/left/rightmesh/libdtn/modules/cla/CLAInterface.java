package io.left.rightmesh.libdtn.modules.cla;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * A CLAInterface is an abstraction of the underlying protocol used as a CLA.
 *
 * @author Lucien Loiseau on 16/10/18.
 */
public interface CLAInterface {

    /**
     * Set the logger
     *
     * @param logger
     */
    void setLogger(Log logger);

    /**
     * The name for this CLA must be the exact same one that is used in a EIDCLA to identify
     * this Convergence Layer Adapter.
     *
     * @return a String with the name of this CLA.
     */
    String getCLAName();

    /**
     * When a CLA is started it should return an Observable of CLAChannel used to actually send
     * and receive bundles.
     *
     * @return Flowable of Bundle
     */
    Observable<CLAChannel> start();

    /**
     * When a CLA is stopped, it should stop creating any new CLAChannel and terminate the
     * observable. It is an implementation specific decision wether or not to close all the
     * underlying CLAChannels that were previously openned.
     */
    void stop();

    /**
     * Tries to open a channel to the given CLA-specific EID.
     *
     * @param eid of the peer to open a channel too, must be CLA-specific
     * @return Single of CLAChannel if successful, error otherwise
     */
    Single<CLAChannel> open(CLA eid);

}
