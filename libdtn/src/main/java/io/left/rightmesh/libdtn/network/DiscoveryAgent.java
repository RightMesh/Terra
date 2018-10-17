package io.left.rightmesh.libdtn.network;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.events.ChannelClosed;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.utils.Log;
import io.left.rightmesh.librxbus.RxBus;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_COMPONENT_DETECT_PEER_ON_LAN;

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
public class DiscoveryAgent extends Component {


    private static final String TAG = "DiscoveryAgent";

    // ---- SINGLETON ----
    private static DiscoveryAgent instance;

    public static DiscoveryAgent getInstance() {
        return instance;
    }

    static {
        instance = new DiscoveryAgent();
        instance.initComponent(ENABLE_COMPONENT_DETECT_PEER_ON_LAN);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        LibDetect.start(4000, new ActionListener() {
            @Override
            public void onPeerReachable(PeerReachable peer) {
                Log.i(TAG, "peer detected :" + peer.address.getHostAddress());
                ConnectionAgent.createOpportunityLibDetect(peer.address.getHostAddress()).subscribe(
                        channel -> {
                            RxBus.post(new ChannelOpened(channel));
                            channel.recvBundle().subscribe(
                                    b -> {
                                        /* ignore for the moment */
                                        Log.i(TAG, channel.channelEID()+" -> received a new bundle from " + b.source.getEIDString());
                                        BundleProcessor.bundleReception(b);
                                    },
                                    e -> RxBus.post(new ChannelClosed(channel)),
                                    () -> RxBus.post(new ChannelClosed(channel)));
                        },
                        e -> {
                            /* ignore */
                        }
                );
            }

            @Override
            public void onPeerUnreachable(PeerUnreachable peer) {
                Log.i(TAG, "peer unreachable");
            }
        }, true);
    }

    @Override
    protected void componentDown() {
        super.componentDown();
    }
}
