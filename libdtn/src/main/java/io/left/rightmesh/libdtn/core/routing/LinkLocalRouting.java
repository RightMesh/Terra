package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtncommon.data.eid.CLA;
import io.left.rightmesh.libdtncommon.data.eid.DTN;
import io.left.rightmesh.libdtn.events.ChannelClosed;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.events.LinkLocalEntryDown;
import io.left.rightmesh.libdtn.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtncommon.data.eid.EID;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.HashSet;
import java.util.Set;

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
        linkLocalTable = new HashSet<>();
        instance.initComponent(COMPONENT_ENABLE_LINKLOCAL_ROUTING);
    }

    private static Set<CLAChannel> linkLocalTable;

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        RxBus.unregister(this);
    }

    public static void channelOpened(CLAChannel channel) {
        if(linkLocalTable.add(channel)) {
            RxBus.post(new LinkLocalEntryUp(channel));
        }
    }

    public static void channelClosed(CLAChannel channel) {
        if(linkLocalTable.remove(channel)) {
            RxBus.post(new LinkLocalEntryDown(channel));
        }
    }

    public static CLA isEIDLinkLocal(EID eid) {
        if(!getInstance().isEnabled()) {
            return null;
        }

        for(CLAChannel cla : linkLocalTable) {
            if(eid.matches(cla.localEID())) {
                return cla.localEID();
            }
        }
        return null;
    }

    public static Maybe<CLAChannel> findCLA(EID destination) {
        if(!getInstance().isEnabled()) {
            Maybe.error(new Throwable(TAG+" is disabled"));
        }

        return Observable.fromIterable(linkLocalTable)
                .filter(c -> destination.matches(c.channelEID()))
                .lastElement();
    }

    @Subscribe
    public void onEvent(ChannelOpened event) {
        channelOpened(event.channel);
    }

    @Subscribe
    public void onEvent(ChannelClosed event) {
        channelOpened(event.channel);
    }



    // todo remove this
    public static String print() {
        StringBuilder sb = new StringBuilder("Link-Local Table:\n");
        sb.append("--------------\n\n");
        linkLocalTable.forEach((entry) -> {
            String remote = entry.channelEID().getEIDString();
            String local  = entry.localEID().getEIDString();
            String mode;
            if(entry.getMode().equals(CLAChannel.ChannelMode.InUnidirectional)) {
                mode = " <-- ";
            } else if (entry.getMode().equals(CLAChannel.ChannelMode.OutUnidirectional)) {
                mode = " --> ";
            } else {
                mode = " <-> ";
            }
            sb.append(local + mode +remote+"\n");
        });
        sb.append("\n");
        return sb.toString();
    }
}
