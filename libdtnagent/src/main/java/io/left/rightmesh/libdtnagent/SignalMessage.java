package io.left.rightmesh.libdtnagent;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;

/**
 * @author Lucien Loiseau on 13/10/18.
 */
public class SignalMessage implements CborParser.ParseableItem {

    public enum SignalType {
        DATA_AVAILABLE
    }

    SignalType type;
    String sink;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_parse_int((__, ___, i) -> {
                    switch ((int) i) {
                        case 0:
                            type = SignalType.DATA_AVAILABLE;
                            break;
                        default:
                            throw new RxParserException("wrong signal type");
                    }})
                .cbor_parse_text_string_full((__, str) -> sink = str);
    }

}
