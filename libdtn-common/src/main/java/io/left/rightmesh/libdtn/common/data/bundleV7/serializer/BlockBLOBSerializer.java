package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class BlockBLOBSerializer {

    static CborEncoder encode(BlockBLOB block) {
        return CBOR.encoder()
                .cbor_encode_byte_string(block.data.observe());
    }
}
