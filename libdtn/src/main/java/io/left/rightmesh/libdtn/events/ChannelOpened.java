package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.network.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class ChannelOpened implements DTNEvent {

    public static final String id = "ChannelOpened";

    public CLAChannel channel;

    public ChannelOpened(CLAChannel channel) {
        this.channel = channel;
    }

    @Override
    public String getID() {
        return id;
    }
}
