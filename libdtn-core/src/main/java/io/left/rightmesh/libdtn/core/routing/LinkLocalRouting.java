package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryDown;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.modules.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.HashSet;
import java.util.Set;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_LINKLOCAL_ROUTING;

/**
 * LinkLocalRouting is the link-local routing linkLocalTable. It contains all the linklocal EID
 * associated with their CLAChannelSPI.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting extends BaseComponent {

    private static final String TAG = "LinkLocalRouting";

    public LinkLocalRouting(DTNCore core) {
        linkLocalTable = new HashSet<>();
        initComponent(core.getConf(), COMPONENT_ENABLE_LINKLOCAL_ROUTING, core.getLogger());
    }

    private Set<CLAChannelSPI> linkLocalTable;

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        RxBus.unregister(this);
    }

    public void channelOpened(CLAChannelSPI channel) {
        if(linkLocalTable.add(channel)) {
            RxBus.post(new LinkLocalEntryUp(channel));
        }
    }

    public void channelClosed(CLAChannelSPI channel) {
        if(linkLocalTable.remove(channel)) {
            RxBus.post(new LinkLocalEntryDown(channel));
        }
    }

    public CLA isEIDLinkLocal(EID eid) {
        if(!isEnabled()) {
            return null;
        }

        for(CLAChannelSPI cla : linkLocalTable) {
            if(eid.matches(cla.localEID())) {
                return cla.localEID();
            }
        }
        return null;
    }

    public Maybe<CLAChannelSPI> findCLA(EID destination) {
        if(!isEnabled()) {
            return Maybe.error(new Throwable(TAG+" is disabled"));
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
        channelClosed(event.channel);
    }


    // todo remove this
    public String print() {
        StringBuilder sb = new StringBuilder("Link-Local Table:\n");
        sb.append("--------------\n\n");
        linkLocalTable.forEach((entry) -> {
            String remote = entry.channelEID().getEIDString();
            String local  = entry.localEID().getEIDString();
            String mode;
            if(entry.getMode().equals(CLAChannelSPI.ChannelMode.InUnidirectional)) {
                mode = " <-- ";
            } else if (entry.getMode().equals(CLAChannelSPI.ChannelMode.OutUnidirectional)) {
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
