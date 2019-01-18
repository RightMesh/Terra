package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * ManifestBlockParser parses the data-specific part of the ManifestBlock block.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class ManifestBlockParser {

    static CborParser getParser(ManifestBlock block, Log logger) {
        return CBOR.parser(); //todo
    }

}
