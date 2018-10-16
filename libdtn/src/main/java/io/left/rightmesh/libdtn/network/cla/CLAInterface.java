package io.left.rightmesh.libdtn.network.cla;

import io.reactivex.Observable;

/**
 * A CLAInterface is an abstraction of the underlying protocol used as a CLA.
 *
 * @author Lucien Loiseau on 16/10/18.
 */
public interface CLAInterface {

    /**
     * When a CLA is started it should return an Observable of CLAChannel used to actually send
     * and receive bundles.
     *
     * @return Flowable of Bundle
     */
    Observable<CLAChannel> start();

    /**
     * When a CLA is stopped, it should stop returning any new CLAChannel. It is an implementation
     * specific decision wether or not to close all the underlying CLAChannels that were previously
     * openned.
     */
    void stop();

}
