package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.data.EID;

/**
 * This event is thrown whenever a DTNChannel is open.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class ChannelOpened extends DTNEvents {
    public EID eid;
    public DTNChannel channel;

    public ChannelOpened(EID eid, DTNChannel channel) {
        this.eid = eid;
        this.channel = channel;
    }
}
