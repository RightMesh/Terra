package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.network.Peer;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * The convergence layer refers to the link-layer specific of the RFC5050.
 *
 * @author Lucien Loiseau on 20/08/18.
 */
public interface ConvergenceLayer {

    /**
     * starts the Convergence Layer. It returns a stream of DTNChannel for evert new channel opens.
     *
     * @return Observable of DTNChannel
     */
    Observable<DTNChannel> start();

    /**
     * Stop the convergence layer. This method may or may not close all the channel opened
     * with peers. Once stopped, this ConvergenceLayer cannot accept nor create new Channels.
     */
    void stop();
}
