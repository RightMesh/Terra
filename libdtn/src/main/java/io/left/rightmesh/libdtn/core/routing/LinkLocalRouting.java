package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.data.EID;

import java.util.HashMap;
import java.util.Map;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_LINKLOCAL_ROUTING;

/**
 * LinkLocalRouting is the link-local routing linkLocalTable. It contains all the linklocal EID
 * associated with their CLAChannel.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting extends Component {

    // ---- SINGLETON ----
    private static LinkLocalRouting instance = new LinkLocalRouting();
    public static LinkLocalRouting getInstance() {  return instance; }
    public static void init() {}

    private Map<EID, CLAChannel> linkLocalTable = new HashMap<>();

    public LinkLocalRouting() {
        super(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    public static void channelOpened(CLAChannel channel) {
        getInstance().linkLocalTable.put(channel.channelEID(), channel);
    }

    public static void channelClosed(CLAChannel channel) {
        getInstance().linkLocalTable.remove(channel.channelEID());
    }

    public static CLAChannel findCLA(Bundle bundle) {
        if(!getInstance().isEnabled()) {
            return null;
        }

        for(EID peer : getInstance().linkLocalTable.keySet()) {
            if(bundle.destination.matches(peer)) {
                return getInstance().linkLocalTable.get(peer);
            }
        }

        return null;
    }

    public static CLAChannel isPeerLinkLocal(EID eid) {
        if(getInstance().linkLocalTable.containsKey(eid)) {
            return getInstance().linkLocalTable.get(eid);
        }
        return null;
    }
}
