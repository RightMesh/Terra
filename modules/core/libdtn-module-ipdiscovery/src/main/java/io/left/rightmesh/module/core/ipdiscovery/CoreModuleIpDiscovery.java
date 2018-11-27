package io.left.rightmesh.module.core.ipdiscovery;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;


/**
 * The role of the discovery agent is to discover the local peers on all the interface  available
 * and throws appropriate Events whenever there is a change to the topology. It should be able to
 * detect Bluetooth neighbor, WiFi neighbor and neighbor over Internet link (such as superpeers).
 *
 * <p>The Discovery Agent, basically simply acts as a scanner. Any actual connection decision are
 * taken by the ConnectionAgent.
 *
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
                api.getConnectionAgent().createOpportunityLibDetect(peer.address.getHostAddress()).subscribe(
                        channel -> {
                            /* ignore */
                        },
                        e -> {
                            /* ignore */
                        }
                );
            }

            @Override
            public void onPeerUnreachable(PeerUnreachable peer) {
                api.getLogger().i(TAG, "peer unreachable");
            }
        }, true);
    }
}