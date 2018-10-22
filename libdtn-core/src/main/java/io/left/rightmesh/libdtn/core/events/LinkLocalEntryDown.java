package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.modules.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryDown {
    public CLAChannel channel;

    public LinkLocalEntryDown(CLAChannel channel) {
        this.channel = channel;
    }
}
