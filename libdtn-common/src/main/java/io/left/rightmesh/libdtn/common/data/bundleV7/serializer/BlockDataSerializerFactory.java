package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * @author Lucien Loiseau on 21/11/18.
 */
public interface BlockDataSerializerFactory {

    class UnknownBlockTypeException extends Exception{
    }
    /**
     * returns a serializer for the given ExtensionBlock
     * @param block extension block to serialize
     * @return CborEncoder
     * @throws UnknownBlockTypeException if type is unknown
     */
    CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException;
}
