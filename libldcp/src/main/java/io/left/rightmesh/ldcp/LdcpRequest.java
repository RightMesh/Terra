package io.left.rightmesh.ldcp;

import io.left.rightmesh.ldcp.messages.RequestMessage;
import io.left.rightmesh.ldcp.messages.ResponseMessage;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.librxtcp.RxTCP;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public class LdcpRequest {

    public static final String TAG = "LdcpRequest";

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

    public LdcpRequest setBody(String body) {
        requestMessage.body = body;
        return this;
    }

    public Single<ResponseMessage> send(String host, int port, ExtensionToolbox toolbox, BlobFactory factory, Log logger) {
        return Single.create(s -> new RxTCP.ConnectionRequest<>(host, port)
                .connect()
                .subscribe(
                        c -> {
                            logger.d(TAG, "connected to: "+host+":"+port);
                            c.order(requestMessage.encode()).track().ignoreElements().subscribe(
                                    () -> {
                                        logger.d(TAG, "request sent, waiting for response");
                                        c.recv().subscribe(
                                                buf -> {
                                                    CborParser parser = ResponseMessage.getParser(logger, toolbox, factory);
                                                    try {
                                                        while (buf.hasRemaining() && !parser.isDone()) {
                                                            if(parser.read(buf)) {
                                                                c.closeNow();
                                                                s.onSuccess(parser.getReg(0));
                                                            }
                                                        }
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
                                                });
                                    },
                                    e -> {
                                        c.closeNow();
                                        s.onError(e);
                                    }
                            );
                        },
                        s::onError
                ));
    }

}
