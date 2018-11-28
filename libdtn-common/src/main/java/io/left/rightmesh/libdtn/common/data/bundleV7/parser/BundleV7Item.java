package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Item implements CborParser.ParseableItem {

    static final String TAG = "BundleV7Item";

    public BundleV7Item(Log logger,
                        ExtensionToolbox toolbox,
                        BLOBFactory blobFactory) {
        this.logger = logger;
        this.toolbox = toolbox;
        this.blobFactory = blobFactory;
    }

    public Bundle bundle = null;
    private Log logger;
    private ExtensionToolbox toolbox;
    private BLOBFactory blobFactory;


    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array((__, ___, ____) -> {
                    logger.v(TAG, "[+] parsing new bundle");
                })
                .cbor_parse_custom_item(
                        () -> new PrimaryBlockItem(toolbox.getEIDFactory(), logger),
                        (__, ___, item) -> {
                    logger.v(TAG, "-> primary block parsed");
                    bundle = item.b;
                })
                .cbor_parse_array_items(
                        () -> new CanonicalBlockItem(logger, toolbox, blobFactory),
                        (__, ___, item) -> {
                    logger.v(TAG, "-> canonical block parsed");

                    /* early validation of block */
                    try {
                        toolbox.getBlockProcessorFactory().create(item.block.type).onBlockDeserialized(item.block);
                    } catch(BlockProcessorFactory.ProcessorNotFoundException pnfe) {
                        /* ignore */
                    } catch(ProcessingException pe) {
                        throw new RxParserException(pe.getMessage());
                    }

                    bundle.addBlock(item.block);
                });
    }
}
