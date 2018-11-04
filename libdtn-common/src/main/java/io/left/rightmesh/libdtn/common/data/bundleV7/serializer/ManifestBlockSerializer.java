package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class ManifestBlockSerializer {


    static CborEncoder encode(ManifestBlock block) {
        return CBOR.encoder();
    }

}
