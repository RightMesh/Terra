package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryDown;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_LINKLOCAL_ROUTING;

/**
 * LinkLocalRouting is the link-local routing linkLocalTable. It contains all the linklocal EID
 * associated with their CLAChannelSPI.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting extends BaseComponent {

    private static final String TAG = "LinkLocalRouting";

    public LinkLocalRouting(DTNCore core) {
        this.core = core;
        linkLocalTable = new HashSet<>();
        initComponent(core.getConf(), COMPONENT_ENABLE_LINKLOCAL_ROUTING, core.getLogger());
    }

    private Set<CLAChannelSPI> linkLocalTable;
    private DTNCore core;

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

    void channelOpened(CLAChannelSPI channel) {
        if(linkLocalTable.add(channel)) {
            channel.recvBundle(core.getStorage().getBlobFactory()).subscribe(
                    b -> {
                        core.getLogger().i(TAG, "channel "
                                + channel.channelEID().getEIDString()
                                + " received a new bundle from "
                                + b.source.getEIDString());
                        b.tag("cla-origin-iid", channel.channelEID());
                        core.getBundleProcessor().bundleReception(b);
                    },
                    e -> channelClosed(channel),
                    () -> channelClosed(channel));
            RxBus.post(new LinkLocalEntryUp(channel));
        }
    }

    void channelClosed(CLAChannelSPI channel) {
        if(linkLocalTable.remove(channel)) {
            RxBus.post(new LinkLocalEntryDown(channel));
        }
    }

    CLA isEIDLinkLocal(EID eid) {
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

    Maybe<CLAChannelSPI> findCLA(EID destination) {
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

    Set<CLAChannelSPI> dumpTable() {
        return Collections.unmodifiableSet(linkLocalTable);
    }

}
