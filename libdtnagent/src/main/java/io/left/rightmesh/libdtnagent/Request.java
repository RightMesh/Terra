package io.left.rightmesh.libdtnagent;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;

/**
 * @author Lucien Loiseau on 12/10/18.
 */
public class Request {

    public enum RequestCode {
        REGISTER(0),
        UNREGISTER(1),
        GET(2),
        POST(3);

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

    public int version;
    public RequestCode code;

    public static CborParser factoryParser() {
        return CBOR.parser()
                .cbor_parse_int((__, ___, i) -> {/* version */})
                .cbor_parse_int((p, ___, i) -> {
                    RequestCode code = RequestCode.fromId((int) i);
                    p.save("code", code);
                    switch (code) {
                        case GET:
                            p.save("message", new Get());
                            break;
                        case POST:
                            p.save("message", new Post());
                            break;
                        case REGISTER:
                            p.save("message", new Register());
                            break;
                        case UNREGISTER:
                            p.save("message", new Unregister());
                            break;
                        default:
                            throw new RxParserException("wrong request code");
                    }})
                .do_insert_if(
                        (p) -> p.<RequestCode>get("code").equals(RequestCode.REGISTER),
                        CBOR.parser().cbor_parse_text_string_full(
                                (p, str) -> p.<Register>get("message").sink = str))
                .do_insert_if(
                        (p) -> p.<RequestCode>get("code").equals(RequestCode.UNREGISTER),
                        CBOR.parser().cbor_parse_text_string_full(
                                (p, str) -> p.<Unregister>get("message").sink = str))
                .do_insert_if(
                        (p) -> p.<RequestCode>get("code").equals(RequestCode.GET),
                        CBOR.parser().cbor_parse_text_string_full(
                                (p, str) -> p.<Get>get("message").sink = str)
                        .cbor_parse_int(
                                (p, ___, i) -> p.<Get>get("message").number = (int)i))
                .do_insert_if(
                        (p) -> p.<RequestCode>get("code").equals(RequestCode.POST),
                        CBOR.parser().cbor_parse_text_string_full(
                                (p, str) -> p.<Post>get("message").destination = str
                        ).cbor_parse_text_string_full(
                                        (p, str) -> p.<Post>get("message").reportto = str));
    }


    public static class Register extends Request {
        String sink;
    }


    public static class Unregister extends Request {
        String sink;
    }

    public static class Get extends Request {
        String sink;
        int    number;
    }

    public static class Post extends Request {
        String destination;
        String reportto;
    }

}
