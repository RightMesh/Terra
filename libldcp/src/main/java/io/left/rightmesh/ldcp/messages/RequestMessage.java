package io.left.rightmesh.ldcp.messages;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.encoder.CborEncodingUnknown;
import io.left.rightmesh.libcbor.parser.RxParserException;
import io.left.rightmesh.libcbor.parser.items.TextStringItem;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * LDCP RequestMessage data structure.
 *
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

        /**
         * return the code that matches an Id.
         *
         * @param id of the request
         * @return RequestCode
         */
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

    /**
     * Encode an RequestMessage in its CBOR representation.
     *
     * @return encoded message
     */
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
        } catch (CborEncodingUnknown ceu) {
            return Flowable.error(ceu);
        }
    }

    /**
     * get the parser to parse an encoded request message.
     *
     * @param logger      logger
     * @param toolbox     toolbox to parse bundle
     * @param blobFactory blob factory to parse bundle
     * @return parser
     */
    public static CborParser getParser(Log logger,
                                       ExtensionToolbox toolbox,
                                       BlobFactory blobFactory) {
        return CBOR.parser()
                .cbor_parse_int((p, t, i) -> { /* version */
                })
                .cbor_parse_int((p, t, i) -> {
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
                        TextStringItem::new,
                        TextStringItem::new,
                        (p, ___, map) -> {
                            RequestMessage req = p.getReg(0);
                            for (TextStringItem str : map.keySet()) {
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
