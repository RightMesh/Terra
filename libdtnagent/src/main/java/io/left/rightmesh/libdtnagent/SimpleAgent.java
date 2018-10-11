package io.left.rightmesh.libdtnagent;

import io.left.rightmesh.librxtcp.RxTCP;

/**
 * @author Lucien Loiseau on 11/10/18.
 */
public class SimpleAgent {
    private static final String TAG = "SimpleAgent";

    public void start() {
        new RxTCP.ConnectionRequest("127.0.0.1",4557)
                .connect()
                .subscribe(
                        Session::new,
                        e -> {/* daemon is not started */}
                );
    }

    public static class Session {
        RxTCP.Connection c;
        Session(RxTCP.Connection con) {
            this.c = con;
        }
    }
}