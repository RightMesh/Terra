package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.utils.Log;

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

    private static final String TAG = "LinkLocalRouting";

    // ---- SINGLETON ----
    private static LinkLocalRouting instance;
    public static LinkLocalRouting getInstance() {  return instance; }
    static {
        instance = new LinkLocalRouting();
        instance.initComponent(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    private Map<EID, CLAChannel> linkLocalTable = new HashMap<>();

    @Override
    protected String getComponentName() {
        return TAG;
    }

    public static void channelOpened(CLAChannel channel) {
        getInstance().linkLocalTable.put(channel.channelEID(), channel);
    }

    public static void channelClosed(CLAChannel channel) {
        getInstance().linkLocalTable.remove(channel.channelEID());
    }

    public static CLAChannel findCLA(EID destination) {
        if(!getInstance().isEnabled()) {
            return null;
        }

        for(EID peer : getInstance().linkLocalTable.keySet()) {
            if(destination.matches(peer)) {
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
