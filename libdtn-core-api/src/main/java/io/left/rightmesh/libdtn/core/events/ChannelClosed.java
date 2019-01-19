package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;

/**
 * ChannelClosed event is thrown whenever a {@link ClaChannelSpi} has closed.
 *
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelClosed implements DtnEvent {
    public ClaChannelSpi channel;

    public ChannelClosed(ClaChannelSpi channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Channel closed: local=" + channel.localEid().getEidString()
                + " peer=" + channel.channelEid().getEidString();
    }
}
