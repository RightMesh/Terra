package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.api.cla.CLAChannelSPI;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class LinkLocalEntryDown {
    public CLAChannelSPI channel;

    public LinkLocalEntryDown(CLAChannelSPI channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Delete link-local entry: local="+channel.localEID().getEIDString()+" peer="+channel.channelEID().getEIDString();
    }
}
