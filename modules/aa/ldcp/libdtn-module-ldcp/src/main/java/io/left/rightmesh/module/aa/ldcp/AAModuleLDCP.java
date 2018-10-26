package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSPI;

import static io.left.rightmesh.module.aa.ldcp.Configuration.LDCPEntry.LDCP_TCP_PORT;
import static io.left.rightmesh.module.aa.ldcp.Configuration.LDCP_TCP_PORT_DEFAULT;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class AAModuleLDCP implements ApplicationAgentAdapterSPI {

    private static final String TAG = "ldcp";

    Log logger;

    public AAModuleLDCP() {
    }

    @Override
    public String getModuleName() {
        return TAG;
    }

    @Override
    public void init(RegistrarAPI api, ConfigurationAPI conf, Log logger) {
        int port = conf.getModuleConf(this, LDCP_TCP_PORT, LDCP_TCP_PORT_DEFAULT).value();
        logger.i(TAG, "starting a ldcp server on port " + port);
        new LdcpServer().start(port, null, null);
    }


    /*
    private static final String TAG = "APIDaemonLDCPAgent";

    private DTNCore core;
    private RxTCP.Server<RequestChannel> server;
    private final Map<RequestMessage.RequestCode, Action> ACTIONS;

    public APIDaemonLDCPAgent(DTNCore core) {
        initComponent(core.getConf(), COMPONENT_ENABLE_CBOR_DAEMON_API, core.getLogger());
        final HashMap<RequestMessage.RequestCode, Action> actionMap = new HashMap<>();
        actionMap.put(RequestMessage.RequestCode.REGISTER, new RegisterAction());
        actionMap.put(RequestMessage.RequestCode.UNREGISTER, new UnregisterAction());
        actionMap.put(RequestMessage.RequestCode.GET, new GETAction());
        actionMap.put(RequestMessage.RequestCode.POST, new POSTAction());
        ACTIONS = Collections.unmodifiableMap(actionMap);
    }


            recv().subscribe(
                    byteBuffer -> {
                        while (byteBuffer.hasRemaining()) {
                            if (parser.isDone()) {
                                RequestMessage req =  parser.getReg(0);
                                if(req != null) {
                                    ACTIONS.get(req.code).process(req, this);
                                }
                            }
                        }
                    },
                    e -> closeNow(),
                    this::closeNow);
        }

        public void sendResponse(ResponseMessage message) {
        }
    }

    interface Action {
        void process(RequestMessage message, RequestChannel channel);
    }

    private class RegisterAction implements Action {
        public void process(RequestMessage message, RequestChannel channel) {
            System.out.println("register");
        }
    }

    private class UnregisterAction implements Action {
        public void process(RequestMessage message, RequestChannel channel) {
            System.out.println("unregister");
        }
    }

    private class GETAction implements Action {
        public void process(RequestMessage message, RequestChannel channel) {
            System.out.println("get");
        }
    }

    private class POSTAction implements Action {
        public void process(RequestMessage message, RequestChannel channel) {
            System.out.println("post");
        }
    }
*/
}
