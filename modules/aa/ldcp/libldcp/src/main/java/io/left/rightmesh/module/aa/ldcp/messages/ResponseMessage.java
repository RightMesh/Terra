package io.left.rightmesh.module.aa.ldcp.messages;

import java.nio.ByteBuffer;
import java.util.HashMap;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.reactivex.Flowable;

import static io.left.rightmesh.module.aa.ldcp.LdcpAPI.LDCP_VERSION;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class ResponseMessage {

    public enum ResponseCode {
        OK(0),
        ERROR(1);

        int code;

        ResponseCode(int id) {
            this.code = id;
        }

        public static ResponseMessage.ResponseCode fromId(int id) {
            for (ResponseMessage.ResponseCode type : values()) {
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

    public Flowable<ByteBuffer> encode() {
        try {
            CborEncoder enc = CBOR.encoder()
                    .cbor_encode_int(LDCP_VERSION)
                    .cbor_encode_int(code.code)
                    .cbor_encode_map(fields);

            if (bundle != null) {
                enc.cbor_encode_boolean(true)
                        .merge(BundleV7Serializer.encode(bundle));
            } else {
                enc.cbor_encode_boolean(false);
            }
            return enc.observe(1024);
        } catch (CBOR.CborEncodingUnknown ceu) {
            return Flowable.error(ceu);
        }
    }

    public static CborParser getParser(BLOBFactory factory) {
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
                                () -> new BundleV7Parser(new NullLogger(), factory).createBundleItem(),
                                (p2, ___, item) -> {
                                    RequestMessage req = p2.getReg(0);
                                    req.bundle = item.bundle;
                                }));
                    }
                });

    }


}
