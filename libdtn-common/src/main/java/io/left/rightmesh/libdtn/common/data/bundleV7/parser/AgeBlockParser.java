package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class AgeBlockParser {

    static CborParser getParser(AgeBlock block, Log logger) {
        return CBOR.parser()
                .cbor_parse_int((p, __, i) -> block.age = i)
                .do_here((p) -> block.start());
    }

}
