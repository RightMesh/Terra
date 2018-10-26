package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSPI;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class AAModuleLDCP implements ApplicationAgentAdapterSPI {

    private static final String TAG = "AAModuleLDCP";

    @Override
    public void setLogger(Log logger) {

    }

    @Override
    public String getModuleName() {
        return TAG;
    }

    @Override
    public void init(RegistrarAPI api) {

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

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        //int signalPort = (Integer) DTNConfiguration.get(DTNConfiguration.CoreEntry.API_DAEMON_SIGNAL_PORT).value();
        int serverPort = (Integer) core.getConf().get(DTNConfiguration.CoreEntry.API_CBOR_DAEMON_CHANNEL_PORT).value();

        server = new RxTCP.Server<>(serverPort, RequestChannel::new);
        server.start().subscribe(
                c -> {
                },
                e -> {
                },
                () -> {
                });
    }

    @Override
    protected void componentDown() {
        if (server != null) {
            server.stop();
        }
    }

    class RequestChannel extends RxTCP.Connection {
        RequestChannel() {

            CborParser parser = CBOR.parser()
                    .cbor_parse_int((__, ___, i) -> { })
                    .cbor_parse_int((p, ___, i) -> {
                        RequestMessage.RequestCode code = RequestMessage.RequestCode.fromId((int) i);
                        if(code == null) {
                            throw new RxParserException("wrong request code");
                        }
                        final RequestMessage message = new RequestMessage(code);
                        p.setReg(0, message);
                    })
                    .cbor_parse_linear_map(
                            CBOR.TextStringItem::new,
                            CBOR.TextStringItem::new,
                            (p, ___, map) -> {
                                RequestMessage req = p.getReg(0);
                                for (CBOR.TextStringItem str : map.keySet()) {
                                    req.fields.put(str.value(), map.get(str).value());
                                }
                            })
                    .cbor_parse_byte_string(
                            (p, ___, size) -> {
                                RequestMessage req =  p.getReg(0);
                                try {
                                    if (size >= 0) {
                                        req.body = core.getStorage().getBlobFactory().createBLOB((int) size);
                                    } else {
                                        // indefinite length CoreBLOBFactory
                                        req.body = core.getStorage().getBlobFactory().createBLOB(2048); //todo change that
                                    }
                                } catch (CoreBLOBFactory.BLOBFactoryException sfe) {
                                    req.body = new NullBLOB();
                                }
                                p.setReg(1, ((BLOB)req.body).getWritableBLOB());
                            },
                            (p, chunk) -> {
                                WritableBLOB blob =  p.getReg(1);
                                try {
                                    blob.write(chunk);
                                } catch (WritableBLOB.BLOBOverflowException | IOException io) {
                                    blob.close();
                                    p.setReg(1, null);
                                    throw new RxParserException("can't save the body blob");
                                }
                            },
                            (p) -> p.<WritableBLOB>getReg(1).close());


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
