package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;

/**
 * Parser for the {@link AgeBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class AgeBlockParser {

    static CborParser getParser(AgeBlock block, Log logger) {
        return CBOR.parser()
                .cbor_parse_int((parser, tags, i) -> {
                    logger.v(TAG, ".. block age="+i);
                    block.age = i;
                })
                .do_here((parser) -> block.start());
    }

}
