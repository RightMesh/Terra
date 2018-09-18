package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.network.Peer;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 17/09/18.
 */
public class STCP implements ConvergenceLayer {

    @Override
    public void stop() {
    }

    @Override
    public Observable<DTNChannel> listen() {
        return null;
    }

    @Override
    public Single<DTNChannel> open(Peer peer) {
        return null;
    }


    public static class Channel implements DTNChannel {
        @Override
        public EID channelEID() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public Observable<Integer> sendBundle(Bundle bundle) {
            return null;
        }

        @Override
        public Observable<Integer> sendBundles(Flowable<Bundle> upstream) {
            return null;
        }

        @Override
        public Observable<Bundle> recvBundle() {
            return null;
        }
    }
}
