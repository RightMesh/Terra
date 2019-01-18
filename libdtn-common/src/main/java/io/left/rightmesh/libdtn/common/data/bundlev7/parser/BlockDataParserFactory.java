package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * Factory to parse blocks.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockDataParserFactory {

    class UnknownBlockTypeException extends Exception{
    }

    /**
     * returns a parser for newly instantiated ExtensionBlock.
     *
     * @param type block type
     * @param block block to parse
     * @param blobFactory {@link Blob} factory
     * @param eidFactory {@link Eid} factory
     * @param logger logger
     * @return CborParser
     * @throws UnknownBlockTypeException if type is unknown
     */
    CborParser create(int type,
                      CanonicalBlock block,
                      BlobFactory blobFactory,
                      EidFactory eidFactory,
                      Log logger) throws UnknownBlockTypeException;


}
