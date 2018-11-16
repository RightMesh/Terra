package io.left.rightmesh.libdtn.core.network;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.librxbus.RxBus;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN;

/**
 * The role of the discovery agent is to discover the local peers on all the interface  available
 * and throws appropriate Events whenever there is a change to the topology. It should be able to
 * detect Bluetooth neighbor, WiFi neighbor and neighbor over Internet link (such as superpeers).
 *
 * <p>The Discovery Agent, basically simply acts as a scanner. Any actual connection decision are
 * taken by the ConnectionAgent.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public class DiscoveryAgent extends BaseComponent {

    private static final String TAG = "DiscoveryAgent";

    private DTNCore core;

    public DiscoveryAgent(DTNCore core) {
        this.core = core;
        initComponent(core.getConf(), ENABLE_COMPONENT_DETECT_PEER_ON_LAN, core.getLogger());
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        LibDetect.start(4000, new ActionListener() {
            @Override
            public void onPeerReachable(PeerReachable peer) {
                core.getLogger().i(TAG, "peer detected :" + peer.address.getHostAddress());
                core.getConnectionAgent().createOpportunityLibDetect(peer.address.getHostAddress()).subscribe(
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
                core.getLogger().i(TAG, "peer unreachable");
            }
        }, true);
    }

    @Override
    protected void componentDown() {
    }
}
