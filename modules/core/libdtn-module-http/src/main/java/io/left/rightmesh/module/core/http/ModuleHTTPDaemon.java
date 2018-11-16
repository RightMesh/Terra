package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;
import io.left.rightmesh.module.core.http.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import static io.left.rightmesh.module.core.http.Configuration.API_DAEMON_HTTP_API_PORT;
import static io.left.rightmesh.module.core.http.Configuration.API_DAEMON_HTTP_API_PORT_DEFAULT;
import static io.left.rightmesh.module.core.http.nettyrouter.Dispatch.using;

/**
 * @author Lucien Loiseau on 13/10/18.
 */
public class ModuleHTTPDaemon implements CoreModuleSPI {

    private static final String TAG = "http";

    private CoreAPI core;
    private HttpServer<ByteBuf, ByteBuf> server;

    public ModuleHTTPDaemon() {
    }

    @Override
    public String getModuleName() {
        return TAG;
    }

    @Override
    public void init(CoreAPI api) {
        this.core = api;
        RequestConfiguration configurationAPI = new RequestConfiguration(core);
        RequestRegistration registrationAPI = new RequestRegistration(core);
        RequestNetwork networkAPI = new RequestNetwork(core);
        RequestStorage storageAPI = new RequestStorage(core);
        RequestBundle applicationAgentAPI = new RequestBundle(core);
        int serverPort = core.getConf().getModuleConf(getModuleName(),
                API_DAEMON_HTTP_API_PORT, API_DAEMON_HTTP_API_PORT_DEFAULT).value();
        api.getLogger().i(TAG, "starting a http server on port "+serverPort);
        server = HttpServer.newServer(serverPort)
                .start(using(new Router<ByteBuf, ByteBuf>()
                        .GET("/", rootAction)
                        .GET("/help", rootAction)
                        .ANY("/conf/", configurationAPI.confAction)
                        .ANY("/conf/:*", configurationAPI.confAction)
                        .ANY("/registration/", registrationAPI.registerAction)
                        .ANY("/network/", networkAPI.networkAction)
                        .ANY("/network/:*", networkAPI.networkAction)
                        .ANY("/cache/", storageAPI.cacheAction)
                        .ANY("/cache/:*", storageAPI.cacheAction)
                        .ANY("/aa/", applicationAgentAPI.aaAction)
                        .ANY("/aa/:*", applicationAgentAPI.aaAction)
                        .notFound(handler404)));
    }

    protected void close() {
        if (server != null) {
            server.shutdown();
        }
    }

    private Action rootAction = (params, req, res) -> {
        String[] header = {
                "         *                                                   .             \n",
                "      +. | .+         .                  .              .         .        \n",
                "  .*.... O   .. *        .                     .       .             .     \n",
                "   ...+.. `'O ..                                    .           |          \n",
                "   +.....+  | ..+                    .                  .      -*-         \n",
                "   ...+...  O ..      ---========================---       .    |          \n",
                "   *...  O'` ...*             .          .                   .             \n",
                " .    O'`  .+.    _________ ____   _____    _____ .  ___                   \n",
                "       `'O       /___  ___// __/  / __  |  / __  |  /   |                  \n",
                "   .                / /.  / /_   / /_/ /  / /_/ /  / /| | .                \n",
                "     .             / /   / __/  / _  |   / _  |   / __  |          .       \n",
                "              .   /./   / /__  / / | |  / / | |  / /  | |                  \n",
                "   |             /_/   /____/ /_/  |_| /_/  |_| /_/   |_|      .           \n",
                "  -*-                                                                *     \n",
                "   |     .           ---========================---             .          \n",
                "      .                 Terrestrial DTN - v1.0     .                    .  \n",
                "          .    .             *                    .             .          \n",
                "                                 .                         .               \n",
                "____ /\\__________/\\____ ______________/\\/\\___/\\__________________________|@\n",
                "                __                                               ---       \n",
                "         --           -            --  -      -         ---  __            \n",
                "   --  __                      ___--     RightMesh (c) 2018        --  __  \n\n",
                "REST API Available: \n",
                "------------------- \n\n",
                "/register/{sink}\n",
                "/unregister/{sink}\n",
                "/fetch/{nb}/{sink}\n",
                "/table/\n",
                "/table/registration/\n",
                "/table/registration/add/{sink}\n",
                "/table/registration/del/{sink}\n",
                "/table/linklocal/\n",
                "/table/static/\n",
                "/table/static/add/{eid-to}/{eidcla-nexthop}/\n",
                "/table/static/del/{eid-to}/{eidcla-nexthop}/\n",
                "/cache/\n",
                "/cache/size/\n",
                "/cache/destination/{eid}\n",
                "/cache/source/{eid}\n",
                "/cache/registration/{eid}\n",
                "/conf/\n",
                "/conf/localeid/\n",
                "/conf/localeid/{eid}\n",
                "/conf/aliases/\n",
                "/conf/aliases/add/{eid}\n",
                "/conf/aliases/del/{eid}\n",
                "/conf/maxlifetime/{max}\n",
                "/conf/maxtimestampfuture/{max}\n",
                "/conf/recvanonymous/{enable|disable}\n",
                "/conf/statusreporting/{enable|disable}\n",
                "/conf/forwarding/{enable|disable}\n",
                "/conf/blocksize/{max}\n",
                ""};
        return res.setStatus(HttpResponseStatus.OK)
                .writeString(Observable.from(header));
    };

    private Action handler404 = (params, req, res) ->
            res.setStatus(HttpResponseStatus.NOT_FOUND);
}
