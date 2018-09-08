package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.bundleV6.EID;

/**
 * This event is thrown whenever a DTNChannel has closed.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class ChannelClosed {
    public EID eid;

    public ChannelClosed(EID eid) {
        this.eid = eid;
    }
}
