package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;

/**
 * ChannelOpened event is thrown whenever a {@link ClaChannelSpi} was opened.
 *
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelOpened implements DtnEvent {
    public ClaChannelSpi channel;

    public ChannelOpened(ClaChannelSpi channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Channel opened: local=" + channel.localEid().getEidString()
                + " peer=" + channel.channelEid().getEidString();
    }
}
