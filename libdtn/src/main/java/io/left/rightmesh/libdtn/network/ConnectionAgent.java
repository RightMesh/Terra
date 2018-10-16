package io.left.rightmesh.libdtn.network;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.reactivex.Maybe;

/**
 * The ConnectionAgent takes decision as to how to connect/disconnect and generally affect the
 * network topology. It may react to event thrown by the DiscoveryAgent but can also be actively
 * invoked by other module to create a new connection, for instance to a server on the Internet.
 *
 * @author Lucien Loiseau on 15/10/18.
 */
public class ConnectionAgent extends Component {
    private static final String TAG = "ConnectionAgent";

    // ---- SINGLETON ----
    private static ConnectionAgent instance;
    public static ConnectionAgent getInstance() {  return instance; }
    static {
        instance = new ConnectionAgent();
        instance.initComponent(DTNConfiguration.Entry.COMPONENT_ENABLE_CONNECTION_AGENT);
    }



    @Override
    public String getComponentName() {
        return TAG;
    }


    /**
     * Try to create an opportunity for this destination.
     * @param destination
     */
    public static Maybe<CLAChannel> createOpportunity(EID.CLA destination) {
        if(!instance.isEnabled()) {
            return Maybe.error(new Throwable(TAG+" is disabled"));
        }

        if(!DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_AUTO_CONNECT).value()) {
            return Maybe.error(new Throwable("AutoConnect is disabled"));
        }

        return Maybe.empty();
    }
}
