package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * ScopeControlHopLimitBlockParser parses the data-specific of a ScopeControlHopLimitBlock.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class ScopeControlHopLimitBlockParser {

    static CborParser getParser(ScopeControlHopLimitBlock block, Log logger) {
        return CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((p, t, i) -> block.count = i)
                .cbor_parse_int((p, t, i) -> block.limit = i);
    }

}
