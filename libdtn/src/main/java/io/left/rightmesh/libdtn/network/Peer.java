package io.left.rightmesh.libdtn.network;

import io.left.rightmesh.libdtncommon.data.eid.EID;

/**
 * Peer class holds information about a peer, that is, a unique DTN device.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public abstract class Peer {

    public abstract EID getEID();

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Peer) {
            Peer peer = (Peer) o;
            return this.getEID().equals(peer.getEID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getEID().hashCode();
    }

    @Override
    public String toString() {
        return "eid=" + getEID().toString();
    }

}
