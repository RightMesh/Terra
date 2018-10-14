package io.left.rightmesh.libdtn.core.agents;

import java.io.IOException;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.utils.nettyrouter.Route;
import io.left.rightmesh.libdtn.utils.nettyrouter.Router;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_DAEMON_HTTP_API;
import static io.left.rightmesh.libdtn.utils.nettyrouter.Dispatch.using;
import static rx.Observable.just;

/**
 * @author Lucien Loiseau on 13/10/18.
 */
public class APIDaemonHTTPAgent extends Component {


    private static final String TAG = "APIDaemonHTTPAgent";

    // ---- SINGLETON ----
    private static APIDaemonHTTPAgent instance = new APIDaemonHTTPAgent();

    public static APIDaemonHTTPAgent getInstance() {
        return instance;
    }

    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_DAEMON_HTTP_API);
    }

    HttpServer<ByteBuf, ByteBuf> server;

    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        int serverPort = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.API_DAEMON_HTTP_API_PORT).value();
        server = HttpServer.newServer(serverPort)
                .start(using(new Router<ByteBuf, ByteBuf>()
                        .GET("/", rootAction)
                        .GET("/register", registerAction)
                        .GET("/register/:*", registerAction)
                        .GET("/unregister/", unregisterAction)
                        .GET("/unregister/:*", unregisterAction)
                        .GET("/fetch/", fetchAction)
                        .GET("/fetch/:*", fetchAction)
                        .GET("/conf/", confAction)
                        .GET("/conf/:*", confAction)
                        .GET("/cache/", cacheAction)
                        .GET("/cache/:*", cacheAction)
                        .notFound(handler404)));
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        if (server != null) {
            server.shutdown();
        }
    }

    private interface Action extends Route<ByteBuf, ByteBuf> {
    }

    private static Action registerAction = (params, req, res) -> {
        String sink = params.get("*");
        if (sink == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("incorrect sink"))
                    .writeString(just(RegistrationTable.printTable()));
        }
        if (RegistrationTable.register(sink)) {
            return res.setStatus(HttpResponseStatus.OK)
                    .writeString(just("sink registered: " + sink))
                    .writeString(just(RegistrationTable.printTable()));
        } else {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("sink registered: " + sink))
                    .writeString(just(RegistrationTable.printTable()));
        }
    };

    private static Action unregisterAction = (params, req, res) -> {
        String sink = params.get("*");
        if (sink == null) {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("incorrect sink"))
                    .writeString(just(RegistrationTable.printTable()));
        }
        if (RegistrationTable.unregister(sink)) {
            return res.setStatus(HttpResponseStatus.OK)
                    .writeString(just("sink unregistered: " + sink))
                    .writeString(just(RegistrationTable.printTable()));
        } else {
            return res.setStatus(HttpResponseStatus.BAD_REQUEST)
                    .writeString(just("no such sink: " + sink))
                    .writeString(just(RegistrationTable.printTable()));
        }
    };

    private static Action fetchAction = (params, req, res) -> {
        return res.writeString(just("fetch"));
    };

    private static Action confAction = (params, req, res) -> {
        return res.setStatus(HttpResponseStatus.OK).writeString(just("conf"));
    };

    private static Action cacheAction = (params, req, res) -> {
        return res.setStatus(HttpResponseStatus.OK).writeString(just("cache"));
    };

    private static Action rootAction = (params, req, res) -> {
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
        return res.setStatus(HttpResponseStatus.OK).writeString(Observable.from(header));
    };

    private static Action handler404 = (params, req, res) ->
            res.setStatus(HttpResponseStatus.NOT_FOUND);
}
