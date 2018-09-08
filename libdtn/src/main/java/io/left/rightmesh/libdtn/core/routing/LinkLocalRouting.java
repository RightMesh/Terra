package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.bus.RxBus;
import io.left.rightmesh.libdtn.bus.Subscribe;
import io.left.rightmesh.libdtn.events.ChannelClosed;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.data.EID;

import java.util.HashMap;
import java.util.Map;

/**
 * LinkLocalRouting is the link-local routing table. It contains all the linklocal EID
 * associated with their DTNChannel.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting {

    private Map<EID, DTNChannel> table;
    private boolean up;

    public LinkLocalRouting() {
        initialize();
    }

    /**
     * Initialize the component. It reads the DTNConfiguration and can reactively
     * turn on or off.
     */
    public void initialize() {
        DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_LINKLOCAL_ROUTING).observe()
                .subscribe(
                        enabled -> {
                            if (enabled) {
                                componentUp();
                            } else {
                                componentDown();
                            }
                        });
    }

    private void componentUp() {
        if (up) {
            return;
        }
        up = true;

        table = new HashMap<>();
        RxBus.register(this);
    }

    private void componentDown() {
        if (!up) {
            return;
        }
        up = false;

        RxBus.unregister(this);
        table.clear();
    }

    public boolean isUp() {
        return up;
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
