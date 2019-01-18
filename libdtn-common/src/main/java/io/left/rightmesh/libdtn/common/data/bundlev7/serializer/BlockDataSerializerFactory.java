package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * Factory that creates a block-specific serializer for a given block.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockDataSerializerFactory {

    class UnknownBlockTypeException extends Exception{
    }

    /**
     * returns a serializer for the given ExtensionBlock.
     *
     * @param block extension block to serialize
     * @return CborEncoder
     * @throws UnknownBlockTypeException if PAYLOAD_BLOCK_TYPE is unknown
     */
    CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException;
}
