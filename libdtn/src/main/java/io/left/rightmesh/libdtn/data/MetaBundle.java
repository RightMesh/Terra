package io.left.rightmesh.libdtn.data;

import java.util.concurrent.atomic.AtomicLong;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;

/**
 * @author Lucien Loiseau on 07/10/18.
 */
public class MetaBundle {
    public BundleID bid;
    public EID destination;
    public EID source;
    public long bundle_size;

    public MetaBundle() {
    }

    public MetaBundle(MetaBundle meta) {
        this.bid = meta.bid;
        this.destination = meta.destination;
        this.source = meta.source;
        this.bundle_size = meta.bundle_size;
    }

    public MetaBundle(Bundle bundle) {
        bid = bundle.bid;
        destination = bundle.destination;
        source = bundle.source;
        AtomicLong size = new AtomicLong();
        BundleV7Serializer.encode(bundle).observe()
                .subscribe(
                        buffer -> size.set(size.get() + buffer.remaining()),
                        e -> bundle_size = -1,
                        () -> bundle_size = size.get());
    }

    public MetaBundle(Bundle bundle, CborEncoder bundleEncoder) {
        bid = bundle.bid;
        destination = bundle.destination;
        source = bundle.source;
        AtomicLong size = new AtomicLong();
        bundleEncoder.observe()
                .subscribe(
                        buffer -> size.set(size.get() + buffer.remaining()),
                        e -> bundle_size = -1,
                        () -> bundle_size = size.get());
    }


    public CborEncoder encode() {
        return CBOR.encoder()
                .cbor_start_array(4)
                .cbor_encode_text_string(bid.toString())
                .cbor_encode_text_string(destination.toString())
                .cbor_encode_text_string(source.toString())
                .cbor_encode_int(bundle_size);
    }

    public static class MetaBundleItem implements CborParser.ParseableItem {

        public MetaBundle meta;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(4)
                    .do_here((__) -> meta = new MetaBundle())
                    .cbor_parse_text_string_full(
                            (__, str) -> meta.bid = new BundleID(str))
                    .cbor_parse_text_string_full(
                            (__, str) -> {
                                try {
                                    meta.destination = EID.create(str);
                                } catch (EID.EIDFormatException efe) {
                                    meta.destination = EID.NullEID();
                                }
                            })
                    .cbor_parse_text_string_full(
                            (__, str) -> {
                                try {
                                    meta.source = EID.create(str);
                                } catch (EID.EIDFormatException efe) {
                                    meta.source = EID.NullEID();
                                }
                            })
                    .cbor_parse_int((__, ___, i) -> meta.bundle_size = i);
        }
    }

}
