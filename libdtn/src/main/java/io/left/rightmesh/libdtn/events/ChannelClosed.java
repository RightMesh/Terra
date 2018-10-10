package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.network.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelClosed implements DTNEvent {
    public CLAChannel channel;

    public ChannelClosed(CLAChannel channel) {
        this.channel = channel;
    }
}
