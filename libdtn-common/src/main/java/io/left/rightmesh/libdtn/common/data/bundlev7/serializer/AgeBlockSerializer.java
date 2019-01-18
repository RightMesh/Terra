package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AgeBlock;

/**
 * AgeBlockSerializer serializes an {@link AgeBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class AgeBlockSerializer {

    /**
     * serializes an {@link AgeBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized AgeBlock.
     */
    static CborEncoder encode(AgeBlock block) {
        block.stop();
        long age = block.age + block.timeEnd - block.timeStart;
        return CBOR.encoder()
                .cbor_encode_int(age);
    }

}
