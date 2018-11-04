package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class PreviousNodeBlockSerializer {

    static CborEncoder encode(PreviousNodeBlock block) {
        return EIDSerializer.encode(block.previous);
    }

}
