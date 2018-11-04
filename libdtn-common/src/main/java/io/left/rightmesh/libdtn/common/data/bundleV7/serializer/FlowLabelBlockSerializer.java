package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class FlowLabelBlockSerializer {

    static CborEncoder encode(FlowLabelBlock block) {
        return CBOR.encoder();
    }

}
