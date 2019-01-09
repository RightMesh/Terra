package io.left.rightmesh.module.core.ipdiscovery;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;


/**
 * @author Lucien Loiseau on 27/11/18.
 */
public class CoreModuleIpDiscovery implements CoreModuleSPI {

    private static final String TAG = "IpDiscovery";

    @Override
    public String getModuleName() {
        return "ipdiscovery";
    }

    @Override
    public void init(CoreAPI api) {
        LibDetect.start(4000, new ActionListener() {
            @Override
            public void onPeerReachable(PeerReachable peer) {
                api.getLogger().i(TAG, "peer detected :" + peer.address.getHostAddress());
                /*
                api.getConnectionAgent().createOpportunityLibDetect(peer.address.getHostAddress()).subscribe(
                        channel -> {
                            // ignore
                        },
                        e -> {
                            // ignore
                        }
                );
                */
            }

            @Override
            public void onPeerUnreachable(PeerUnreachable peer) {
                api.getLogger().i(TAG, "peer unreachable");
            }
        }, true);
    }

    /*
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
                .doOnSuccess((c) -> {
                    core.getLogger().d(TAG, "opportunity creation success: " + opp);
                    RxBus.post(new ChannelOpened(c));
                });
    }
    */
}