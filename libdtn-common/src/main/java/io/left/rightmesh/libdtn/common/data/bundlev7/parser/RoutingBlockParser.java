package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.RoutingBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * Parser for the {@link RoutingBlock}.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public class RoutingBlockParser {

    static CborParser getParser(RoutingBlock block, Log logger) {
        return CBOR.parser(); //todo
    }

}

