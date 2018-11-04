package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AgeBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class AgeBlockSerializer {

    static CborEncoder encode(AgeBlock block) {
        block.stop();
        long age = block.age + block.time_end - block.time_start;
        return CBOR.encoder()
                .cbor_encode_int(age);
    }

}
