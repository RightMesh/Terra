package io.left.rightmesh.libdtn.network;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.data.eid.CLA;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.network.cla.CLAManager;
import io.left.rightmesh.libdtn.utils.Log;

import io.reactivex.Single;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_CONNECTION_AGENT;

/**
 * The ConnectionAgent takes decision as to how to connect/disconnect and generally affect the
 * network topology. It may react to event thrown by the DiscoveryAgent but can also be actively
 * invoked by other module to create a new connection, for instance to a server on the Internet.
 *
 * @author Lucien Loiseau on 15/10/18.
 */
public class ConnectionAgent {

    private static final String TAG = "ConnectionAgent";

    public static boolean isEnabled() {
        return DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_CONNECTION_AGENT).value();
    }

    /**
     * Try to create an opportunity for this destination.
     * @param eid
     */
    public static Single<CLAChannel> createOpportunity(CLA eid) {
        if(!isEnabled()) {
            return Single.error(new Throwable(TAG+" is disabled"));
        }

        if(!DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_AUTO_CONNECT).value()) {
            return Single.error(new Throwable("AutoConnect is disabled"));
        }

        final String opp = "cla=" + eid.getCLAName() + " peer=" + eid.getCLASpecificPart();
        Log.d(TAG, "trying to create an opportunity with "+opp);
        return CLAManager.openChannel(eid)
                .doOnError(e -> Log.d(TAG, "opportunity creation failed: " + opp))
                .doOnSuccess((c) -> Log.d(TAG, "opportunity creation success: " + opp));
    }
}
