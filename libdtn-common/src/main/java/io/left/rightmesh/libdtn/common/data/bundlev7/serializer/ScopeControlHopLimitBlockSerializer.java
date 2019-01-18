package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;

/**
 * ScopeControlHopLimitBlockSerializer serializes a {@link ScopeControlHopLimitBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class ScopeControlHopLimitBlockSerializer {

    /**
     * serializes a {@link ScopeControlHopLimitBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized ScopeControlHopLimitBlock.
     */
    static CborEncoder encode(ScopeControlHopLimitBlock block) {
        return CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(block.count)
                .cbor_encode_int(block.limit);
    }

}
