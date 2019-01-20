package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.RoutingBlock;

/**
 * RoutingBlockSerializer serializes a {@link RoutingBlock}.
 * @author Lucien Loiseau on 19/01/19.
 */
public class RoutingBlockSerializer {

    /**
     * serializes a {@link RoutingBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized ManifestBlock.
     */
    static CborEncoder encode(RoutingBlock block) {
        return CBOR.encoder();
    }

}
