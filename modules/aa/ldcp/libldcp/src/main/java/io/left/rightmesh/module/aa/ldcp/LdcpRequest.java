package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.librxtcp.RxTCP;
import io.left.rightmesh.module.aa.ldcp.messages.RequestMessage;
import io.left.rightmesh.module.aa.ldcp.messages.ResponseMessage;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public class LdcpRequest {

    private RequestMessage requestMessage;

    private LdcpRequest(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    public static LdcpRequest GET(String path) {
        RequestMessage message = new RequestMessage(RequestMessage.RequestCode.GET);
        message.path = path;
        return new LdcpRequest(message);
    }

    public static LdcpRequest POST(String path) {
        RequestMessage message = new RequestMessage(RequestMessage.RequestCode.POST);
        message.path = path;
        return new LdcpRequest(message);
    }

    public LdcpRequest setHeader(String field, String value) {
        requestMessage.fields.put(field, value);
        return this;
    }

    public LdcpRequest setBundle(Bundle bundle) {
        requestMessage.bundle = bundle;
        return this;
    }

    public Single<ResponseMessage> send(String host, int port) {
        return Single.create(s -> new RxTCP.ConnectionRequest<>(host, port)
                .connect()
                .subscribe(
                        c -> {
                            c.send(requestMessage.encode());

                            BundleV7Parser bundleParser = new BundleV7Parser(new NullLogger());
                            CborParser parser = CBOR.parser()
                                    .cbor_parse_int((__, ___, i) -> {
                                    })
                                    .cbor_parse_int((p, ___, i) -> {
                                        ResponseMessage.ResponseCode code = ResponseMessage.ResponseCode.fromId((int) i);
                                        if (code == null) {
                                            throw new RxParserException("wrong request code");
                                        }
                                        final ResponseMessage message = new ResponseMessage(code);
                                        p.setReg(0, message);
                                    })
                                    .cbor_parse_linear_map(
                                            CBOR.TextStringItem::new,
                                            CBOR.TextStringItem::new,
                                            (p, ___, map) -> {
                                                ResponseMessage res = p.getReg(0);
                                                for (CBOR.TextStringItem str : map.keySet()) {
                                                    res.fields.put(str.value(), map.get(str).value());
                                                }
                                            })
                                    .cbor_parse_custom_item(
                                            bundleParser::createBundleItem,
                                            (p, ___, item) -> {
                                                ResponseMessage res = p.getReg(0);
                                                res.bundle = item.bundle;
                                            });

                            c.recv().subscribe(
                                    buf -> {
                                        try {
                                            while (buf.hasRemaining() && !parser.isDone()) {
                                                parser.read(buf);
                                            }
                                            c.closeNow();
                                            s.onSuccess(parser.getReg(0));
                                        } catch (RxParserException rpe) {
                                            c.closeNow();
                                            s.onError(rpe);
                                        }
                                    },
                                    e -> {
                                        c.closeNow();
                                        s.onError(e);
                                    },
                                    () -> {
                                        c.closeNow();
                                        s.onError(new Throwable("no response"));
                                    }
                            );

                        },
                        s::onError
                ));
    }

}
