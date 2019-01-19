package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.CoreComponent;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.LinkLocalRoutingApi;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryDown;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * LinkLocalRouting is the link-local routing linkLocalTable. It contains all the linklocal Eid
 * associated with their ClaChannelSpi.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class LinkLocalRouting extends CoreComponent implements LinkLocalRoutingApi {

    private static final String TAG = "LinkLocalRouting";

    private Set<ClaChannelSpi> linkLocalTable;
    private CoreApi core;

    public LinkLocalRouting(CoreApi core) {
        this.core = core;
        linkLocalTable = new HashSet<>();
    }

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

    private void channelOpened(ClaChannelSpi channel) {
        if (linkLocalTable.add(channel)) {
            channel.recvBundle(
                    core.getExtensionManager(),
                    core.getStorage().getBlobFactory())
                    .subscribe(
                            b -> {
                                core.getLogger().i(TAG, "channel "
                                        + channel.channelEid().getEidString()
                                        + " received a new bundle from "
                                        + b.getSource().getEidString());
                                b.tag("cla-origin-iid", channel.channelEid());
                                core.getBundleProcessor().bundleReception(b);
                            },
                            e -> channelClosed(channel),
                            () -> channelClosed(channel));
            RxBus.post(new LinkLocalEntryUp(channel));
        }
    }

    private void channelClosed(ClaChannelSpi channel) {
        if (linkLocalTable.remove(channel)) {
            RxBus.post(new LinkLocalEntryDown(channel));
        }
    }

    @Override
    public ClaEid isEidLinkLocal(Eid eid) {
        if (!isEnabled()) {
            return null;
        }

        for (ClaChannelSpi cla : linkLocalTable) {
            if (eid.matches(cla.localEid())) {
                return cla.localEid();
            }
        }
        return null;
    }

    @Override
    public Maybe<ClaChannelSpi> findCla(Eid destination) {
        if (!isEnabled()) {
            return Maybe.error(new ComponentIsDownException(TAG));
        }

        return Observable.fromIterable(linkLocalTable)
                .filter(c -> destination.matches(c.channelEid()))
                .lastElement();
    }

    @Override
    public Set<ClaChannelSpi> dumpTable() {
        return Collections.unmodifiableSet(linkLocalTable);
    }


    @Subscribe
    public void onEvent(ChannelOpened event) {
        channelOpened(event.channel);
    }

    @Subscribe
    public void onEvent(ChannelClosed event) {
        channelClosed(event.channel);
    }
}
