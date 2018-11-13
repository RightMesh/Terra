package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelClosed implements DTNEvent {
    public CLAChannelSPI channel;

    public ChannelClosed(CLAChannelSPI channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Channel closed: local="+channel.localEID().getEIDString()+" peer="+channel.channelEID().getEIDString();
    }
}
