package io.left.rightmesh.aa.ldcp.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.module.aa.ldcp.LdcpRequest;
import io.left.rightmesh.module.aa.ldcp.LdcpServer;
import io.left.rightmesh.module.aa.ldcp.Router;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Single;

import static io.left.rightmesh.aa.ldcp.api.APIPaths.DELIVER;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.DISPATCH;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.FETCHBUNDLE;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.GETBUNDLE;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.ISREGISTERED;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.REGISTER;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.UNREGISTER;
import static io.left.rightmesh.aa.ldcp.api.APIPaths.UPDATE;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class ApplicationAgent implements ApplicationAgentAPI {

    private static final String TAG = "ldcp-api";

    private String host;
    private int port;
    private LdcpServer server;
    private BlobFactory factory;
    private ExtensionToolbox toolbox;
    private Log logger;
    ActiveRegistrationCallback cb;

    public ApplicationAgent(String host, int port, ExtensionToolbox toolbox, BlobFactory factory) {
        this(host, port, toolbox, factory, new NullLogger());
    }

    public ApplicationAgent(String host, int port, ExtensionToolbox toolbox, BlobFactory factory, Log logger) {
        this.host = host;
        this.port = port;
        this.toolbox = toolbox;
        this.factory = factory;
        this.logger = logger;
    }

    private boolean startServer(ActiveRegistrationCallback cb) {
        if (server != null) {
            return false;
        }

        server = new LdcpServer();
        server.start(0, toolbox, factory, logger,
                Router.create()
                        .POST(DELIVER,
                                (req, res) -> cb.recv(req.bundle)
                                        .doOnComplete(() -> res.setCode(ResponseMessage.ResponseCode.OK))
                                        .doOnError(e -> res.setCode(ResponseMessage.ResponseCode.ERROR))));
        return true;
    }

    private boolean stopServer() {
        if (server == null) {
            return false;
        }
        server.stop();
        return true;
    }

    @Override
    public Single<Boolean> isRegistered(String sink) {
        return LdcpRequest.GET(ISREGISTERED)
                .setHeader("sink", sink)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<String> register(String sink) {
        return register(sink, null);
    }

    @Override
    public Single<String> register(String sink, ActiveRegistrationCallback cb) {
        if (startServer(cb)) {
            return LdcpRequest.POST(REGISTER)
                    .setHeader("sink", sink)
                    .setHeader("active", cb == null ? "false" : "true")
                    .setHeader("active-host", "127.0.0.1")
                    .setHeader("active-port", "" + server.getPort())
                    .send(host, port, toolbox, factory, logger)
                    .flatMap(res -> {
                        if (res.code == ResponseMessage.ResponseCode.ERROR) {
                            return Single.error(new RegistrarException(res.body));
                        }
                        if (res.fields.get("cookie") == null) {
                            return Single.error(new RegistrarException("no cookie received"));
                        }
                        return Single.just(res.fields.get("cookie"));
                    });
        } else {
            return Single.error(new RegistrationAlreadyActive());
        }
    }

    @Override
    public Single<Boolean> unregister(String sink, String cookie) {
        return LdcpRequest.POST(UNREGISTER)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Set<BundleId> checkInbox(String sink, String cookie) {
        return null;
    }

    @Override
    public Single<Bundle> get(String sink, String cookie, BundleId bundleID) {
        return LdcpRequest.GET(GETBUNDLE)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleID.getBidString())
                .send(host, port, toolbox, factory, logger)
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
    public Single<Bundle> fetch(String sink, String cookie, BundleId bundleID) {
        return LdcpRequest.GET(FETCHBUNDLE)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleID.getBidString())
                .send(host, port, toolbox, factory, logger)
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
        return LdcpRequest.POST(DISPATCH)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setBundle(bundle)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<Boolean> send(Bundle bundle) {
        return LdcpRequest.POST(DISPATCH)
                .setBundle(bundle)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<Boolean> reAttach(String sink, String cookie, ActiveRegistrationCallback cb) {
        if (startServer(cb)) {
            return LdcpRequest.POST(UPDATE)
                    .setHeader("sink", sink)
                    .setHeader("cookie", cookie)
                    .setHeader("active", "true")
                    .setHeader("active-host", "127.0.0.1")
                    .setHeader("active-port", "" + server.getPort())
                    .send(host, port, toolbox, factory, logger)
                    .flatMap(res -> {
                        if (res.code == ResponseMessage.ResponseCode.ERROR) {
                            return Single.error(new RegistrarException(res.body));
                        }
                        return Single.just(true);
                    });
        } else {
            return Single.error(new RegistrationAlreadyActive());
        }
    }

    @Override
    public Single<Boolean> setPassive(String sink, String cookie) {
        stopServer();
        return LdcpRequest.POST(UPDATE)
                .setHeader("active", "false")
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .send(host, port, toolbox, factory, logger)
                .flatMap(res -> {
                    if (res.code == ResponseMessage.ResponseCode.ERROR) {
                        return Single.error(new RegistrarException(res.body));
                    }
                    return Single.just(true);
                });
    }
}
