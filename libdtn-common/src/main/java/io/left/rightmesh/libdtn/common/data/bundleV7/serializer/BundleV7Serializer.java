package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Serializer {

    public static final byte BUNDLE_VERSION_7 = 0x07;

    /**
     * Turn a Bundle into a serialized Flowable of ByteBuffer.
     *
     * @param bundle to serialize
     * @return Flowable
     */
    public static CborEncoder encode(Bundle bundle) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_indefinite_array()
                .merge(PrimaryBlockSerializer.encode(bundle));

        for (CanonicalBlock block : bundle.getBlocks()) {
            enc.merge(CanonicalBlockSerializer.encode(block));
        }

        enc.cbor_stop_array();
        return enc;
    }

}
