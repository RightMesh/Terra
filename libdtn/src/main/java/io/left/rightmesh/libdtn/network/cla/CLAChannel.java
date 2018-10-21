package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtncommon.data.Bundle;
import io.left.rightmesh.libdtncommon.data.eid.CLA;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * A CLAChannel is an abstraction of the underlying transport protocol used by a CLA and should be
 * able to receive and send DTN Bundles.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public interface CLAChannel {

    enum ChannelMode {
        OutUnidirectional,
        InUnidirectional,
        BiDirectional
    }

    ChannelMode getMode();

    /**
     * return the EID specific for this Channel. It must be unique accross all channels.
     * It is used to identify this interface.
     *
     * @return EID of this channel
     */
    CLA channelEID();

    /**
     * return the EID that represents the local host for this specific Channel.
     *
     * @return EID of this channel
     */
    CLA localEID();

    /**
     * Receive the deserialized stream of Bundle from this Convergence Layer.
     *
     * @return Flowable of Bundle
     */
    Observable<Bundle> recvBundle();

    /**
     * Send a Bundle.
     * todo add priority
     *
     * @param bundle to send
     * @return an Observable to track the number of bytes sent.
     */
    Observable<Integer> sendBundle(Bundle bundle);

    /**
     * Send a stream of Bundles.
     *
     * @param upstream the stream of bundle to be sent
     * @return an Observable to track the number of bundle sent.
     */
    Observable<Integer> sendBundles(Flowable<Bundle> upstream);

    /**
     * Close that channel. Once a channel is closed, it cannot receive nor send Bundles.
     */
    void close();

}