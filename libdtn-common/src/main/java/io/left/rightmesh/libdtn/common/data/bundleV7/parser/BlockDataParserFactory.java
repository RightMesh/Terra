package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockDataParserFactory {

    class UnknownBlockTypeException extends Exception{
    }

    /**
     * returns a parser for newly instantiated ExtensionBlock.
     *
     * @param type block type
     * @param block extension block
     * @param logger logger
     * @return CborParser
     * @throws UnknownBlockTypeException if type is unknown
     */
    CborParser create(int type, CanonicalBlock block, BLOBFactory blobFactory, Log logger) throws UnknownBlockTypeException;


}
