package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * BundleV7Item is a CborParser.ParseableItem for {@link Bundle}.
 *
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Item implements CborParser.ParseableItem {

    static final String TAG = "BundleV7Item";

    /**
     * A Bundle Item requires a toolbox to be able to parse extension {@link Block} and
     * extension {@link Eid}. It also need a BlobFactory to create a new Blob to hold the payload.
     *
     * @param logger      to output parsing information
     * @param toolbox     for the data structure factory
     * @param blobFactory to create blobs.
     */
    public BundleV7Item(Log logger,
                        ExtensionToolbox toolbox,
                        BlobFactory blobFactory) {
        this.logger = logger;
        this.toolbox = toolbox;
        this.blobFactory = blobFactory;
    }

    public Bundle bundle = null;
    private Log logger;
    private ExtensionToolbox toolbox;
    private BlobFactory blobFactory;


    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array((parser, tags, size) -> {
                    logger.v(TAG, "[+] parsing new bundle");
                })
                .cbor_parse_custom_item(
                        () -> new PrimaryBlockItem(toolbox.getEidFactory(), logger),
                        (parser, tags, item) -> {
                            logger.v(TAG, "-> primary block parsed");
                            bundle = item.bundle;
                        })
                .cbor_parse_array_items(
                        () -> new CanonicalBlockItem(logger, toolbox, blobFactory),
                        (parser, tags, item) -> {
                            logger.v(TAG, "-> canonical block parsed");

                            /* early validation of block */
                            try {
                                toolbox.getBlockProcessorFactory().create(item.block.type)
                                        .onBlockDeserialized(item.block);
                            } catch (BlockProcessorFactory.ProcessorNotFoundException pnfe) {
                                /* ignore */
                            } catch (ProcessingException pe) {
                                throw new RxParserException(pe.getMessage());
                            }

                            bundle.addBlock(item.block);
                        });
    }
}
