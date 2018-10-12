package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.utils.Log;
import io.left.rightmesh.librxbus.Subscribe;
import io.left.rightmesh.librxtcp.RxTCP;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_DAEMON_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIDaemonAgent extends Component {

    private static final String TAG = "APIDaemonAgent";

    // ---- SINGLETON ----
    private static APIDaemonAgent instance = new APIDaemonAgent();
    public static APIDaemonAgent getInstance() {  return instance; }
    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_DAEMON_API);
    }

    private RxTCP.Server daemon;

    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        int port = (Integer) DTNConfiguration.get(DTNConfiguration.Entry.API_DAEMON_LISTENING_PORT).value();
        daemon = new RxTCP.Server(port);
        daemon.start().subscribe(
                Client::new, /* observed in new thread */
                e ->  Log.w(TAG, "can't listen on TCP port " + port),
                () -> Log.w(TAG, "server has stopped"));
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        if(daemon != null) {
            daemon.stop();
        }
    }

    public static class Client {
        Client(RxTCP.Connection con) {
            Log.i(TAG, "AA connected: "+con.getRemoteHost()+":"+con.getRemoteHost());
            /* prepare parser */
            CborParser pdu = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_int((__, ___, i) -> {
                        System.out.println("command: "+i);
                    });

            /* consume incoming buffer */
            con.recv().subscribe(
                    buffer -> {
                        try {
                            while(buffer.hasRemaining()) {
                                if (pdu.read(buffer)) {
                                    pdu.reset();
                                }
                            }
                        } catch (RxParserException rpe) {
                            con.closeNow();
                        }
                    },
                    e -> con.closeNow(),
                    () -> con.closeNow());

            /* close the connection if configuration disables Daemon API */
            DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.COMPONENT_ENABLE_DAEMON_API)
                    .observe().subscribe(
                            b -> {
                                if(!b) {
                                    con.closeNow();
                                }
                            });
        }
    }
}
