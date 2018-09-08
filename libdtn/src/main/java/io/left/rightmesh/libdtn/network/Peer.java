package io.left.rightmesh.libdtn.network;

import io.left.rightmesh.libdtn.data.EID;

/**
 * Peer class holds information about a peer, that is, a unique DTN device.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public class Peer {

    private EID eid;

    public Peer(EID eid) {
        this.eid = eid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Peer) {
            Peer peer = (Peer) o;
            return this.eid.equals(peer.eid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.eid.hashCode();
    }

    @Override
    public String toString() {
        return "eid=" + eid.toString();
    }

}
