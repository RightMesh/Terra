package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;

/**
 * LinkLocalEntryDown event is thrown whenever a link-local entry was removed from link-local table.
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryDown {
    public ClaChannelSpi channel;

    public LinkLocalEntryDown(ClaChannelSpi channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Delete link-local entry: local=" + channel.localEid().getEidString()
                + " peer=" + channel.channelEid().getEidString();
    }
}
