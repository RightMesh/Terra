package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * PreviousNodeBlockParser parses the data-specific part of the PreviousNode block.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class PreviousNodeBlockParser {

    static CborParser getParser(PreviousNodeBlock block, EidFactory eidFactory, Log logger) {
        return CBOR.parser()
                .cbor_parse_custom_item(
                        () -> new EidItem(eidFactory, logger),
                        (p, t, item) -> block.previous = item.eid);
    }

}
