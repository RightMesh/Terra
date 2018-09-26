package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtn.network.Peer;

/**
 * @author Lucien Loiseau on 26/09/18.
 */
public abstract class TCPPeer extends Peer {

    public String host;
    public int port;

    public TCPPeer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getTCPAddress() {
        return "tcp://"+host+":"+port;
    }

}
