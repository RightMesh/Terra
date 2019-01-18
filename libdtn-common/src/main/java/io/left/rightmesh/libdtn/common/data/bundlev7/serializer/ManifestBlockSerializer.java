package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;

/**
 * ManifestBlockSerializer serializes a {@link ManifestBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class ManifestBlockSerializer {

    /**
     * serializes a {@link ManifestBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized ManifestBlock.
     */
    static CborEncoder encode(ManifestBlock block) {
        return CBOR.encoder();
    }

}
