package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.events.ChannelClosed;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.cla.CLAManager;
import io.left.rightmesh.libdtn.network.cla.STCP;
import io.left.rightmesh.libdtn.utils.Log;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxtcp.RxTCP;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_CLA_STCP;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public class STCPAgent extends Component {

    private static final String TAG = "STCPAgent";

    // ---- SINGLETON ----
    private static STCPAgent instance;

    public static STCPAgent getInstance() {
        return instance;
    }

    static {
        instance = new STCPAgent();
        instance.initComponent(COMPONENT_ENABLE_CLA_STCP);
    }

    private STCP cla = null;

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        int port = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.CLA_STCP_LISTENING_PORT).value();
        /*
        CLAManager.<STCP>create(STCP.getCLAName())
                .subscribe(cl -> {
                    this.cla = cl;
                    cla.setPort(port);
                    cla.start().subscribe(
                            dtnChannel -> {
                                // a new peer has opened a unicast channel, we receive bundle
                                RxBus.post(new ChannelOpened(dtnChannel));
                                dtnChannel.recvBundle().subscribe(
                                        b -> {
                                            Log.i(TAG, dtnChannel.channelEID().getEIDString() + " -> received a new bundle from: " + b.source.getEIDString()+" to: "+b.destination.getEIDString());
                                            BundleProcessor.bundleReception(b);
                                        },
                                        e -> {
                                            // channel has closed
                                            RxBus.post(new ChannelClosed(dtnChannel));
                                        },
                                        () -> {
                                            // channel has closed
                                            RxBus.post(new ChannelClosed(dtnChannel));
                                        }
                                );
                            },
                            e -> Log.w(TAG, "can't listen on TCP port " + port),
                            () -> Log.w(TAG, "server has stopped"));
                });
*/
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        if (cla != null) {
            cla.stop();
        }
    }
}
