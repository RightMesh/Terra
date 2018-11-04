package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class ScopeControlHopLimitBlockSerializer {

    static CborEncoder encode(ScopeControlHopLimitBlock block) {
        return CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(block.count)
                .cbor_encode_int(block.limit);
    }

}
