package io.left.rightmesh.ldcp.messages;

import java.nio.ByteBuffer;
import java.util.HashMap;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class RequestMessage {

    private static final int LDCP_VERSION = 0x01;

    public enum RequestCode {
        GET(0),
        POST(1);

        int code;

        RequestCode(int id) {
            this.code = id;
        }

        public static RequestCode fromId(int id) {
            for (RequestCode type : values()) {
                if (type.code == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public RequestCode code;
    public String path;
    public HashMap<String, String> fields = new HashMap<>();
    public Bundle bundle;
    public String body = "";

    public RequestMessage(RequestCode code) {
        this.code = code;
    }

    public Flowable<ByteBuffer> encode() {
        try {
            CborEncoder enc = CBOR.encoder()
                    .cbor_encode_int(LDCP_VERSION)
                    .cbor_encode_int(code.code)
                    .cbor_encode_text_string(path)
                    .cbor_encode_map(fields);

            if (bundle != null) {
                enc.cbor_encode_boolean(true)
                        .merge(BundleV7Serializer.encode(bundle,
                                new BaseBlockDataSerializerFactory()));
            } else {
                enc.cbor_encode_boolean(false);
            }

            enc.cbor_encode_text_string(body);

            return enc.observe(1024);
        } catch (CBOR.CborEncodingUnknown ceu) {
            return Flowable.error(ceu);
        }
    }

    public static CborParser getParser(Log logger,
                                       ExtensionToolbox toolbox,
                                       BlobFactory blobFactory) {
        return CBOR.parser()
                .cbor_parse_int((__, ___, i) -> { /* version */
                })
                .cbor_parse_int((p, ___, i) -> {
                    RequestMessage.RequestCode code = RequestMessage.RequestCode.fromId((int) i);
                    if (code == null) {
                        throw new RxParserException("wrong request code");
                    }
                    final RequestMessage message = new RequestMessage(code);
                    p.setReg(0, message);
                })
                .cbor_parse_text_string_full((p, path) -> {
                    RequestMessage req = p.getReg(0);
                    req.path = path;
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
                .cbor_parse_boolean((p1, b) -> {
                    if (b) {
                        p1.insert_now(CBOR.parser().cbor_parse_custom_item(
                                () -> new BundleV7Item(
                                        logger,
                                        toolbox,
                                        blobFactory),
                                (p2, ___, item) -> {
                                    RequestMessage req = p2.getReg(0);
                                    req.bundle = item.bundle;
                                }));
                    }
                })
                .cbor_parse_text_string_full(
                        (p, str) -> {
                            RequestMessage req = p.getReg(0);
                            req.body = str;
                        });
    }
}
