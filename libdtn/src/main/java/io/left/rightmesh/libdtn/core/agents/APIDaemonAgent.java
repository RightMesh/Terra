package io.left.rightmesh.libdtn.core.agents;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtnagent.Request;
import io.left.rightmesh.libdtnagent.ResponseMessage;
import io.left.rightmesh.librxtcp.RxTCP;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_DAEMON_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIDaemonAgent extends Component {

    private static final String TAG = "APIDaemonAgent";

    // ---- SINGLETON ----
    private static APIDaemonAgent instance = new APIDaemonAgent();

    public static APIDaemonAgent getInstance() {
        return instance;
    }

    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_DAEMON_API);
    }

    RxTCP.Server<RequestChannel> server;

    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        //int signalPort = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.API_DAEMON_SIGNAL_PORT).value();
        int serverPort = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.API_DAEMON_CHANNEL_PORT).value();

        server = new RxTCP.Server<>(serverPort, RequestChannel::new);
        server.start().subscribe(
                c ->  {} /* ignore */,
                e ->  {} /* ignore */,
                () -> {} /* ignore */);
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        if (server != null) {
            server.stop();
        }
    }

    class RequestChannel extends RxTCP.Connection {
        RequestChannel() {
            /* prepare parser for message header*/
            CborParser parser = Request.factoryParser();

            /* receive request */
            recv().subscribe(
                    byteBuffer -> {
                        while (byteBuffer.hasRemaining()) {
                            if(parser.isDone()) {

                            } else {
                                parser.read(byteBuffer);
                            }
                        }
                    },
                    e -> closeNow(),
                    this::closeNow);
        }

        public void sendResponse(ResponseMessage message) {
        }
    }

    static final Map<Request.RequestCode, Action> ACTIONS;
    static {
        final HashMap<Request.RequestCode, Action> actionMap = new HashMap<>();
        actionMap.put(Request.RequestCode.REGISTER, new RegisterAction());
        actionMap.put(Request.RequestCode.UNREGISTER, new UnregisterAction());
        actionMap.put(Request.RequestCode.GET, new GETAction());
        actionMap.put(Request.RequestCode.POST, new POSTAction());
        ACTIONS = Collections.unmodifiableMap(actionMap);
    }

    interface Action {
        void processHeader(Request message, RequestChannel channel);

        boolean bodyChuck(ByteBuffer buffer);
    }

    static class RegisterAction implements Action {
        public void processHeader(Request message, RequestChannel channel) {
            System.out.println("register");
        }

        @Override
        public boolean bodyChuck(ByteBuffer buffer) {
            /* ignore */
            return true;
        }
    }

    static class UnregisterAction implements Action {
        public void processHeader(Request message, RequestChannel channel) {
            System.out.println("unregister");
        }

        @Override
        public boolean bodyChuck(ByteBuffer buffer) {
            /* ignore */
            return true;
        }
    }

    static class GETAction implements Action {
        public void processHeader(Request message, RequestChannel channel) {
            System.out.println("get");
        }

        @Override
        public boolean bodyChuck(ByteBuffer buffer) {
            /* ignore */
            return true;
        }
    }

    static class POSTAction implements Action {
        public void processHeader(Request message, RequestChannel channel) {
            System.out.println("post");
        }

        @Override
        public boolean bodyChuck(ByteBuffer buffer) {
            /* ignore */
            return true;
        }
    }

}
