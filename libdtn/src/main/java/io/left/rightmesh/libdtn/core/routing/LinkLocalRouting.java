package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.eid.CLA;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.events.LinkLocalEntryDown;
import io.left.rightmesh.libdtn.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.data.eid.EID;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        linkLocalTable = new ConcurrentHashMap<>();
        instance.initComponent(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    private static Map<CLA, CLAChannel> linkLocalTable;

    @Override
    public String getComponentName() {
        return TAG;
    }

    public static void channelOpened(CLAChannel channel) {
        if(!linkLocalTable.containsKey(channel.channelEID())) {
            linkLocalTable.put(channel.channelEID(), channel);
            RxBus.post(new LinkLocalEntryUp(channel));
        }
    }

    public static void channelClosed(CLAChannel channel) {
        if(linkLocalTable.remove(channel.channelEID()) != null) {
            RxBus.post(new LinkLocalEntryDown(channel));
        }
    }

    public static boolean isEIDLinkLocal(EID eid) {
        if(!getInstance().isEnabled()) {
            return false;
        }

        for(EID key : linkLocalTable.keySet()) {
            if(eid.matches(key)) {
                return true;
            }
        }
        return false;
    }

    public static Maybe<CLAChannel> findCLA(EID destination) {
        if(!getInstance().isEnabled()) {
            Maybe.error(new Throwable(TAG+" is disabled"));
        }

        return Observable.fromIterable(linkLocalTable.keySet())
                .filter(destination::matches)
                .map(linkLocalTable::get)
                .lastElement();
    }
}
