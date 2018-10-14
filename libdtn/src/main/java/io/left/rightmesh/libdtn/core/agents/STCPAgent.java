package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.network.cla.STCP;
import io.left.rightmesh.libdtn.utils.Log;
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

    private RxTCP.Server<STCP.Channel> server;

    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        int port = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.CLA_STCP_LISTENING_PORT).value();
        server = new RxTCP.Server<>(port, () -> new STCP.Channel(false));
        server.start().subscribe(
                dtnChannel -> {
                    // a new peer has opened a unicast channel, we receive bundle
                    dtnChannel.recvBundle().subscribe(
                            BundleProcessor::bundleReception,
                            e ->  { /* channel has closed */ },
                            () -> { /* channel has closed */ }
                    );
                },
                e ->  Log.w(TAG, "can't listen on TCP port " + port),
                () -> Log.w(TAG, "server has stopped"));
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        if(server != null) {
            server.stop();
        }
    }
}
