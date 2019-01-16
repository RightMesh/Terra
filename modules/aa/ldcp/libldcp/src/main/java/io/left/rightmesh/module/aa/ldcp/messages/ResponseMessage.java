package io.left.rightmesh.module.aa.ldcp.messages;

import java.nio.ByteBuffer;
import java.util.HashMap;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class ResponseMessage {

    private static final int LDCP_VERSION = 0x01;

    public enum ResponseCode {
        OK(0),
        ERROR(1);

        int code;

        ResponseCode(int id) {
            this.code = id;
        }

        public static ResponseCode fromId(int id) {
            for (ResponseCode type : values()) {
                if (type.code == id) {
                    return type;
                }
            }
            return null;
        }
    }

    public ResponseCode code;
    public HashMap<String, String> fields = new HashMap<>();

    public Bundle bundle;
    public String body = "";

    public ResponseMessage() {
    }

    public ResponseMessage(ResponseCode code) {
        this.code = code;
    }

    public ResponseMessage setCode(ResponseCode code) {
        this.code = code;
        return this;
    }

    public ResponseMessage setHeader(String field, String value) {
        this.fields.put(field, value);
        return this;
    }

    public ResponseMessage setBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public ResponseMessage setBody(String body) {
        this.body = body;
        return this;
    }

    public Flowable<ByteBuffer> encode() {
        try {
            CborEncoder enc = CBOR.encoder()
                    .cbor_encode_int(LDCP_VERSION)
                    .cbor_encode_int(code.code)
                    .cbor_encode_map(fields);

            // encode bundle if any
            if (bundle != null) {
                enc.cbor_encode_boolean(true)
                        .merge(BundleV7Serializer.encode(bundle,
                                new BaseBlockDataSerializerFactory()));
            } else {
                enc.cbor_encode_boolean(false);
            }

            // encode body
            enc.cbor_encode_text_string(body);

            return enc.observe(1024);
        } catch (CBOR.CborEncodingUnknown ceu) {
            return Flowable.error(ceu);
        }
    }

    public static CborParser getParser(Log logger, ExtensionToolbox toolbox, BLOBFactory factory) {
        return CBOR.parser()
                .cbor_parse_int((__, ___, i) -> { /* version */
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
                .cbor_parse_boolean((p1, b) -> {
                    if (b) {
                        p1.insert_now(CBOR.parser().cbor_parse_custom_item(
                                () -> new BundleV7Item(logger, toolbox, factory),
                                (p2, ___, item) -> {
                                    ResponseMessage res = p2.getReg(0);
                                    res.bundle = item.bundle;
                                }));
                    }
                })
                .cbor_parse_text_string_full(
                        (p, str) -> {
                            ResponseMessage res = p.getReg(0);
                            res.body = str;
                        });

    }


}
