package io.left.rightmesh.libdtnagent;

import io.left.rightmesh.librxtcp.RxTCP;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 11/10/18.
 */
public class SimpleAgent {
    private static final String TAG = "SimpleAgent";

    public static Single<AASession> open(int port) {
        return Single.create(s -> {
            new RxTCP.ConnectionRequest("127.0.0.1", port)
                    .connect()
                    .subscribe(
                            c -> s.onSuccess(new AASession(c)),
                            s::onError);
        });
    }

    public static class AASession {
        RxTCP.Connection c;
        AASession(RxTCP.Connection c) {
            this.c = c;
        }
    }
}