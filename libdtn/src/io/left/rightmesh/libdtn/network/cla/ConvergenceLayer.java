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
     * listen the Convergence Layer. It returns a stream of DTNChannel initiated by remote peers.
     *
     * @return Observable of DTNChannel
     */
    Observable<DTNChannel> listen();

    /**
     * Proactively initiate a connection to a peer.
     *
     * @param peer to open a connection with
     * @return an Opened DTNChannel for this peer
     */
    Single<DTNChannel> open(Peer peer);

    /**
     * Stop the convergence layer. This method may or may not close all the channel opened
     * with peers. Once stopped, this ConvergenceLayer cannot accept nor create new Channels.
     */
    void stop();
}
