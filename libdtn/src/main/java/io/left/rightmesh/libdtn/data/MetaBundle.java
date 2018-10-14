package io.left.rightmesh.libdtn.data;

import java.util.concurrent.atomic.AtomicLong;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;

/**
 * MetaBundle
 * @author Lucien Loiseau on 07/10/18.
 */
public class MetaBundle extends Bundle {

    public MetaBundle() {
    }

    public MetaBundle(MetaBundle meta) {
        super((PrimaryBlock)meta);
    }

    public MetaBundle(Bundle bundle) {
        super((PrimaryBlock)bundle);
    }
}
