package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.librxtcp.RxTCP;
import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class LdcpServer {

    private static final String TAG = "ldcp-server";

    RxTCP.Server<RxTCP.Connection> server;

    public int getPort() {
        return server.getPort();
    }

    public void start(int port, ExtensionToolbox toolbox, BLOBFactory factory, RequestHandler action) {
        start(port, toolbox, factory, new NullLogger(), action);
    }

    public void start(int port, ExtensionToolbox toolbox, BLOBFactory factory, Log logger, RequestHandler action) {
        server = new RxTCP.Server<>(port);
        server.start().subscribe(
                con -> {
                    CborParser parser = RequestMessage.getParser(logger, toolbox, factory);
                    con.recv().subscribe(
                            buf -> {
                                try {
                                    while (buf.hasRemaining() && !parser.isDone()) {
                                        if(parser.read(buf)) {
                                            RequestMessage req = parser.getReg(0);
                                            logger.v(TAG, con.getRemoteHost()+" "+req.code.name()+" "+req.path);
                                            ResponseMessage res = new ResponseMessage();
                                            action.handle(req, res).subscribe(
                                                    () -> LdcpResponse
                                                            .wrap(res)
                                                            .send(con),
                                                    e -> LdcpResponse.ERROR()
                                                            .setBody(e.getMessage())
                                                            .send(con)
                                            );
                                        }
                                    }
                                } catch (RxParserException rpe) {
                                    logger.w(TAG, "request parser exception: "+rpe.getMessage());
                                    con.closeNow();
                                }
                            },
                            e -> con.closeNow(),
                            con::closeNow
                    );

                });
    }

    public void stop() {
        server.stop();
    }

}
