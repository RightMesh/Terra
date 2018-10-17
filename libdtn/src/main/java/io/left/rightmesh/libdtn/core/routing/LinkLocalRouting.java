package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.eid.CLA;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.data.eid.EID;
import io.reactivex.Maybe;
import io.reactivex.Observable;

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
        linkLocalTable = new HashMap<>();
        instance.initComponent(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    private static Map<CLA, CLAChannel> linkLocalTable;

    @Override
    public String getComponentName() {
        return TAG;
    }

    public static void channelOpened(CLAChannel channel) {
        linkLocalTable.put(channel.channelEID(), channel);
    }

    public static void channelClosed(CLAChannel channel) {
        linkLocalTable.remove(channel.channelEID());
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
