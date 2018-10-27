package io.left.rightmesh.module.aa.ldcp;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class LdcpApplicationAgent implements LdcpAPI {

    String host;
    int port;
    LdcpServer server;
    ActiveLdcpRegistrationCallback cb;

    LdcpApplicationAgent(String host, int port, boolean active, ActiveLdcpRegistrationCallback cb) {
        this.host = host;
        this.port = port;
        this.cb = cb;
        if (active) {
            startServer();
        }
    }

    private boolean startServer() {
        if (server != null) {
            return false;
        }
        server = new LdcpServer();
        server.start(0, null,
                Router.create().POST("/deliver/", deliver));
        return true;
    }

    RequestHandler deliver = (req, res) ->
            cb.recv(req.bundle)
            .doOnComplete(() -> res.setCode(ResponseMessage.ResponseCode.OK))
            .doOnError(e -> res.setCode(ResponseMessage.ResponseCode.ERROR));

    private boolean stopServer() {
        if (server == null) {
            return false;
        }
        server.stop();
        return true;
    }

    @Override
    public Single<Boolean> isRegistered(String sink) {
        return LdcpRequest.GET("/isregistered/")
                .setHeader("sink", sink)
                .send(host, port)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<String> register(String sink) {
        return LdcpRequest.POST("/register/")
                .setHeader("sink", sink)
                .setHeader("active", cb == null ? "false" : "true")
                .setHeader("active-host", "127.0.0.1")
                .setHeader("active-port", "" + server.getPort())
                .send(host, port)
                .flatMap(res -> {
                    if (res.code == ResponseMessage.ResponseCode.ERROR) {
                        return Single.error(new RegistrarException());
                    }
                    if (res.fields.get("cookie") == null) {
                        return Single.error(new RegistrarException());
                    }
                    return Single.just(res.fields.get("cookie"));
                });
    }

    @Override
    public Single<Boolean> unregister(String sink, String cookie) {
        return LdcpRequest.POST("/unregister/")
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .send(host, port)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Set<BundleID> checkInbox(String sink, String cookie) {
        return null;
    }

    @Override
    public Single<Bundle> get(String sink, String cookie, BundleID bundleID) {
        return LdcpRequest.GET("/get/bundle/")
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleID.getBIDString())
                .send(host, port)
                .flatMap(res -> {
                    if (res.code == ResponseMessage.ResponseCode.ERROR) {
                        return Single.error(new RegistrarException());
                    }
                    if (res.bundle == null) {
                        return Single.error(new RegistrarException());
                    }
                    return Single.just(res.bundle);
                });
    }

    @Override
    public Single<Bundle> fetch(String sink, String cookie, BundleID bundleID) {
        return LdcpRequest.GET("/fetch/bundle/")
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleID.getBIDString())
                .send(host, port)
                .flatMap(res -> {
                    if (res.code == ResponseMessage.ResponseCode.ERROR) {
                        return Single.error(new RegistrarException());
                    }
                    if (res.bundle == null) {
                        return Single.error(new RegistrarException());
                    }
                    return Single.just(res.bundle);
                });
    }

    @Override
    public Single<Boolean> send(String sink, String cookie, Bundle bundle) {
        return LdcpRequest.POST("/bundle/")
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setBundle(bundle)
                .send(host, port)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<Boolean> setActive(String sink, String cookie, ActiveLdcpRegistrationCallback cb) {
        if(startServer()) {
            return LdcpRequest.POST("/register/active/")
                    .setHeader("sink", sink)
                    .setHeader("cookie", cookie)
                    .setHeader("active-host", "127.0.0.1")
                    .setHeader("active-port", "" + server.getPort())
                    .send(host, port)
                    .map(res -> res.code == ResponseMessage.ResponseCode.OK);
        } else {
            return Single.error(new RegistrationAlreadyActive());
        }
    }

    @Override
    public Single<Boolean> setPassive(String sink, String cookie) {
        if(stopServer()) {
            return LdcpRequest.POST("/register/passive")
                    .setHeader("sink", sink)
                    .setHeader("cookie", cookie)
                    .send(host, port)
                    .map(res -> res.code == ResponseMessage.ResponseCode.OK);
        } else {
            return Single.error(new RegistrationAlreadyActive());
        }
    }
}
