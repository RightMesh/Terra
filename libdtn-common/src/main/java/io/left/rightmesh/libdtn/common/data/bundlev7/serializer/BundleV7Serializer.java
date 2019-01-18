package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;

/**
 * BundleV7Serializer serializes a {@link Bundle}.
 *
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Serializer {

    public static final byte BUNDLE_VERSION_7 = 0x07;

    /**
     * Serialize a bundle into a cbor stream. If an ExtensionBlock is unknown, the block payload
     * will be empty.
     *
     * @param bundle to serialize
     * @param  blockDataSerializerFactory block serializer
     * @return cbor-encoded bundle
     */
    public static CborEncoder encode(Bundle bundle,
                                     BlockDataSerializerFactory blockDataSerializerFactory) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_indefinite_array()
                .merge(PrimaryBlockSerializer.encode(bundle));

        for (CanonicalBlock block : bundle.getBlocks()) {
            enc.merge(CanonicalBlockSerializer.encode(block, blockDataSerializerFactory));
        }

        enc.cbor_stop_array();
        return enc;
    }

}
