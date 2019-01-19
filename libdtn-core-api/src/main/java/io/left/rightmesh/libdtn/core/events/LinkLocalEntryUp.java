package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;

/**
 * LinkLocalEntryUp event is thrown whenever a link-local entry was added to the link-local table.
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryUp {
    public ClaChannelSpi channel;

    public LinkLocalEntryUp(ClaChannelSpi cla) {
        this.channel = cla;
    }

    @Override
    public String toString() {
        return "New link-local entry: local=" + channel.localEid().getEidString()
                + " peer=" + channel.channelEid().getEidString();
    }
}
