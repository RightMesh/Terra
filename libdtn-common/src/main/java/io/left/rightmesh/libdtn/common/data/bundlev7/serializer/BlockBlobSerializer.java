package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockBlob;

/**
 * BlockBlobSerializer serializes an {@link BlockBlob}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockBlobSerializer {

    /**
     * serializes an {@link BlockBlob}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized BlockBlob.
     */
    static CborEncoder encode(BlockBlob block) {
        return CBOR.encoder()
                .cbor_encode_byte_string(block.data.observe());
    }
}
