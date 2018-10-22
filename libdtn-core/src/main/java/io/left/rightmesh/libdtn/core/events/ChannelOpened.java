package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.modules.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelOpened implements DTNEvent {
    public CLAChannel channel;

    public ChannelOpened(CLAChannel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Channel opened: local="+channel.localEID().getEIDString()+" peer="+channel.channelEID().getEIDString();
    }
}
