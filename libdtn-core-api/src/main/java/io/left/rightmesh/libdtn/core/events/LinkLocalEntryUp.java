package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryUp {
    public CLAChannelSPI channel;
    public LinkLocalEntryUp(CLAChannelSPI cla) {
        this.channel = cla;
    }

    @Override
    public String toString() {
        return "New link-local entry: local="+channel.localEID().getEIDString()+" peer="+channel.channelEID().getEIDString();
    }
}
