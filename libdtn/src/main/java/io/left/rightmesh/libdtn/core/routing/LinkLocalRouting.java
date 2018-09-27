package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.bus.RxBus;
import io.left.rightmesh.libdtn.bus.Subscribe;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.events.ChannelClosed;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.data.EID;

import java.util.HashMap;
import java.util.Map;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_LINKLOCAL_ROUTING;

/**
 * LinkLocalRouting is the link-local routing table. It contains all the linklocal EID
 * associated with their DTNChannel.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting extends Component {

    // ---- SINGLETON ----
    private static LinkLocalRouting instance = new LinkLocalRouting();
    public static LinkLocalRouting getInstance() {  return instance; }

    private Map<EID, DTNChannel> table;

    public LinkLocalRouting() {
        super(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    @Override
    protected void componentUp() {
        table = new HashMap<>();
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        RxBus.unregister(this);
        table.clear();
    }

    @Subscribe
    public void onEvent(ChannelOpened event) {
        table.put(event.eid, event.channel);
    }

    @Subscribe
    public void onEvent(ChannelClosed event) {
        table.remove(event.eid);
    }
}
