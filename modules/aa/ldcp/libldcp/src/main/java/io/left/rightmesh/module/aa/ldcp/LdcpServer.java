package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.librxtcp.RxTCP;
import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class LdcpServer {

    RxTCP.Server<RxTCP.Connection> server;

    public int getPort() {
        return server.getPort();
    }

    public void start(int port, BLOBFactory factory, Action action) {
        server = new RxTCP.Server<>(port);
        server.start().subscribe(
                con -> {
                    BundleV7Parser bundleParser = new BundleV7Parser(new NullLogger());
                    CborParser parser = CBOR.parser()
                            .cbor_parse_int((__, ___, i) -> {
                            })
                            .cbor_parse_int((p, ___, i) -> {
                                RequestMessage.RequestCode code = RequestMessage.RequestCode.fromId((int) i);
                                if (code == null) {
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
                            .cbor_parse_custom_item(
                                    bundleParser::createBundleItem,
                                    (p, ___, item) -> {
                                        RequestMessage req = p.getReg(0);
                                        req.bundle = item.bundle;
                                    });

                    con.recv().subscribe(
                            buf -> {
                                try {
                                    while (buf.hasRemaining() && !parser.isDone()) {
                                        parser.read(buf);
                                    }
                                    RequestMessage req = parser.getReg(0);
                                    ResponseMessage res = new ResponseMessage();
                                    action.handle(req, res).subscribe();
                                    con.send(res.encode());
                                } catch (RxParserException rpe) {
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
