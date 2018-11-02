package io.left.rightmesh.libdtn.core.network;

import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.ConnectionAgentAPI;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;

import io.reactivex.Single;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_CONNECTION_AGENT;

/**
 * The ConnectionAgent takes decision as to how to connect/disconnect and generally affect the
 * network topology. It may react to event thrown by the DiscoveryAgent but can also be actively
 * invoked by other module to create a new connection, for instance to a server on the Internet.
 *
 * @author Lucien Loiseau on 15/10/18.
 */
public class ConnectionAgent extends BaseComponent implements ConnectionAgentAPI {

    private static final String TAG = "ConnectionAgent";

    private DTNCore core;

    public ConnectionAgent(DTNCore core) {
        this.core = core;
        initComponent(core.getConf(), COMPONENT_ENABLE_CONNECTION_AGENT, core.getLogger());
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
    }

    @Override
    protected void componentDown() {
    }

    public Single<CLAChannelSPI> createOpportunityLibDetect(String host) {
        if(!core.getConf().<Boolean>get(ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT).value()) {
            return Single.error(new Throwable("AutoConnect is disabled"));
        }

        final CLASTCP eid;
        try { //todo create safe constructor
            eid = new CLASTCP(host, 4556, "/");
        } catch(EID.EIDFormatException efe) {
            return Single.error(efe);
        }
        final String opp = "cla=" + eid.getCLAName() + " peer=" + eid.getCLASpecificPart();
        core.getLogger().d(TAG, "trying to create an opportunity with "+opp+" "+Thread.currentThread().getName());
        return core.getClaManager().openChannel(eid)
                .doOnError(e -> core.getLogger().d(TAG, "opportunity creation failed " + opp +": "+e.getMessage()))
                .doOnSuccess((c) -> core.getLogger().d(TAG, "opportunity creation success: " + opp));
    }

    /**
     * Try to create an opportunity for this destination.
     *
     * @param eid to create an opportunity to
     */
    public Single<CLAChannelSPI> createOpportunityForBundle(CLA eid) {
        if(!isEnabled()) {
            return Single.error(new Throwable(TAG+" is disabled"));
        }

        if(!core.getConf().<Boolean>get(ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE).value()) {
            return Single.error(new Throwable("AutoConnect is disabled"));
        }

        final String opp = "cla=" + eid.getCLAName() + " peer=" + eid.getCLASpecificPart();
        core.getLogger().d(TAG, "trying to create an opportunity with "+opp);
        return core.getClaManager().openChannel(eid)
                .doOnError(e -> core.getLogger().d(TAG, "opportunity creation failed " + opp + ": "+e.getMessage()))
                .doOnSuccess((c) -> core.getLogger().d(TAG, "opportunity creation success: " + opp));
    }
}
