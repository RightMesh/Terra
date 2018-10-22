package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.modules.cla.CLAChannel;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryUp {
    public CLAChannel channel;
    public LinkLocalEntryUp(CLAChannel cla) {
        this.channel = cla;
    }

    @Override
    public String toString() {
        return "New link-local entry: local="+channel.localEID().getEIDString()+" peer="+channel.channelEID().getEIDString();
    }
}
