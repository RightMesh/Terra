package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;

/**
 * PreviousNodeBlockSerializer serializes a {@link PreviousNodeBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class PreviousNodeBlockSerializer {

    /**
     * serializes a {@link PreviousNodeBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized PreviousNodeBlock.
     */
    static CborEncoder encode(PreviousNodeBlock block) {
        return EidSerializer.encode(block.previous);
    }

}
