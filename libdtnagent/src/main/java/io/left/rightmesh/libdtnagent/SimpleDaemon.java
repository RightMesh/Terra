package io.left.rightmesh.libdtnagent;

import io.left.rightmesh.librxtcp.RxTCP;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class SimpleDaemon {

    public void listen() {
        new RxTCP.ConnectionRequest("127.0.0.1",4557)
                .connect()
                .subscribe(
                        AASession::new,
                        e -> {/* daemon is not started */}
                );
    }

    public static class AASession {
        RxTCP.Connection c;
        AASession(RxTCP.Connection c) {
            this.c = c;
        }
    }
}
