package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Item implements CborParser.ParseableItem {

    static final String TAG = "BundleV7Item";

    public BundleV7Item(Log logger, BLOBFactory factory) {
        this.logger = logger;
        this.factory = factory;
    }

    public Bundle bundle = null;
    private Log logger;
    private BLOBFactory factory;


    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array((__, ___, ____) -> {
                    logger.v(TAG, "[+] parsing new bundle");
                })
                .cbor_parse_custom_item(
                        () -> new PrimaryBlockItem(logger, factory),
                        (__, ___, item) -> {
                    logger.v(TAG, "-> primary block parsed");
                    bundle = item.b;
                })
                .cbor_parse_array_items(
                        () -> new CanonicalBlockItem(logger, factory),
                        (__, ___, item) -> {
                    logger.v(TAG, "-> canonical block parsed");
                    bundle.addBlock(item.block);
                });
    }
}
