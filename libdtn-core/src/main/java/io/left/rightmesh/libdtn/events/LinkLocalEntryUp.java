package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.modules.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryUp {
    public CLAChannel channel;
    public LinkLocalEntryUp(CLAChannel cla) {
        this.channel = cla;
    }
}
