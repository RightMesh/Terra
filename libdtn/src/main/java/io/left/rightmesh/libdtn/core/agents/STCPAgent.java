package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.bus.RxBus;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.cla.STCP;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public class STCPAgent extends Component {

    // ---- SINGLETON ----
    private static STCPAgent instance = new STCPAgent();

    public static STCPAgent getInstance() {
        return instance;
    }

    private STCPAgent() {
        super(DTNConfiguration.Entry.COMPONENT_ENABLE_CLA_STCP);
    }

    private STCP stcp = new STCP();

    @Override
    protected void componentUp() {
        int port = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.CLA_STCP_LISTENING_PORT).value();
        stcp.listen(port).subscribe(
                dtnChannel -> {
                    RxBus.post(new ChannelOpened(dtnChannel.channelEID(), dtnChannel));
                    dtnChannel.recvBundle().subscribe(DTNCore::inject);
                },
                e -> {
                    // ignore
                },
                () -> {
                    // ignore
                });
    }

    @Override
    protected void componentDown() {
        stcp.stop();
    }
}
