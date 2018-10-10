package io.left.rightmesh.libdtn.data;

import java.util.concurrent.atomic.AtomicLong;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;

/**
 * MetaBundle
 * @author Lucien Loiseau on 07/10/18.
 */
public class MetaBundle extends Bundle {

    public long bundle_size;

    public MetaBundle() {
        bundle_size = 0;
    }

    public MetaBundle(MetaBundle meta) {
        super((PrimaryBlock)meta);
        this.bundle_size = meta.bundle_size;
    }

    public MetaBundle(Bundle bundle) {
        super((PrimaryBlock)bundle);
        AtomicLong size = new AtomicLong();
        BundleV7Serializer.encode(bundle).observe()
                .subscribe(
                        buffer -> size.set(size.get() + buffer.remaining()),
                        e -> bundle_size = -1,
                        () -> bundle_size = size.get());
    }

    public MetaBundle(Bundle bundle, CborEncoder bundleEncoder) {
        super((PrimaryBlock)bundle);
        AtomicLong size = new AtomicLong();
        bundleEncoder.observe()
                .subscribe(
                        buffer -> size.set(size.get() + buffer.remaining()),
                        e -> bundle_size = -1,
                        () -> bundle_size = size.get());
    }
}
