package io.left.rightmesh.aa.ldcp;

import io.left.rightmesh.aa.api.ActiveRegistrationCallback;
import io.left.rightmesh.aa.api.ApplicationAgentApi;
import io.left.rightmesh.ldcp.LdcpRequest;
import io.left.rightmesh.ldcp.LdcpServer;
import io.left.rightmesh.ldcp.Router;
import io.left.rightmesh.ldcp.messages.ResponseMessage;
import io.left.rightmesh.libdtn.common.BaseExtensionToolbox;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.blob.BaseBlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.reactivex.Single;

import java.util.Set;

/**
 * ApplicationAgent implements ApplicationAgentApi and uses LDCP request over TCP.
 *
 * @author Lucien Loiseau on 25/10/18.
 */
public class ApplicationAgent implements ApplicationAgentApi {

    private static final String TAG = "ldcp-api";

    private String host;
    private int port;
    private LdcpServer server;
    private BlobFactory factory;
    private ExtensionToolbox toolbox;
    private Log logger;

    /**
     * Constructor.
     * @param host host of the LDCP server running on the registrar
     * @param port port of the LDCP server running on the registrar
     */
    public ApplicationAgent(String host,
                            int port) {
        this(host, port, new BaseExtensionToolbox(), new BaseBlobFactory(), new NullLogger());
    }

    /**
     * Constructor.
     * @param host host of the LDCP server running on the registrar
     * @param port port of the LDCP server running on the registrar
     * @param toolbox Blocks and Eids factory
     */
    public ApplicationAgent(String host,
                            int port,
                            ExtensionToolbox toolbox) {
        this(host, port, toolbox, new BaseBlobFactory(), new NullLogger());
    }

    /**
     * Constructor.
     * @param host host of the LDCP server running on the registrar
     * @param port port of the LDCP server running on the registrar
     * @param toolbox Blocks and Eids factory
     * @param factory Blob factory
     */
    public ApplicationAgent(String host,
                            int port,
                            ExtensionToolbox toolbox,
                            BlobFactory factory) {
        this(host, port, toolbox, factory, new NullLogger());
    }

    /**
     * Constructor.
     * @param host host of the LDCP server running on the registrar
     * @param port port of the LDCP server running on the registrar
     * @param toolbox Blocks and Eids factory
     * @param factory Blob factory
     * @param logger logging service
     */
    public ApplicationAgent(String host,
                            int port,
                            ExtensionToolbox toolbox,
                            BlobFactory factory,
                            Log logger) {
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
                        .POST(ApiPaths.DaemonToClientLdcpPathVersion1.DELIVER.path,
                                (req, res) -> cb.recv(req.bundle)
                                        .doOnComplete(() ->
                                                res.setCode(ResponseMessage.ResponseCode.OK))
                                        .doOnError(e ->
                                                res.setCode(ResponseMessage.ResponseCode.ERROR))));
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
        return LdcpRequest.GET(ApiPaths.ClientToDaemonLdcpPathVersion1.ISREGISTERED.path)
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
            return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.REGISTER.path)
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
        return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.UNREGISTER.path)
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
    public Single<Bundle> get(String sink, String cookie, BundleId bundleId) {
        return LdcpRequest.GET(ApiPaths.ClientToDaemonLdcpPathVersion1.GETBUNDLE.path)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleId.getBidString())
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
    public Single<Bundle> fetch(String sink, String cookie, BundleId bundleId) {
        return LdcpRequest.GET(ApiPaths.ClientToDaemonLdcpPathVersion1.FETCHBUNDLE.path)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setHeader("bundle-id", bundleId.getBidString())
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
        return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.DISPATCH.path)
                .setHeader("sink", sink)
                .setHeader("cookie", cookie)
                .setBundle(bundle)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<Boolean> send(Bundle bundle) {
        return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.DISPATCH.path)
                .setBundle(bundle)
                .send(host, port, toolbox, factory, logger)
                .map(res -> res.code == ResponseMessage.ResponseCode.OK);
    }

    @Override
    public Single<Boolean> reAttach(String sink, String cookie, ActiveRegistrationCallback cb) {
        if (startServer(cb)) {
            return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.UPDATE.path)
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
        return LdcpRequest.POST(ApiPaths.ClientToDaemonLdcpPathVersion1.UPDATE.path)
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
