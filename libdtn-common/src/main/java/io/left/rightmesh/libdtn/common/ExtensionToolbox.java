package io.left.rightmesh.libdtn.common;

import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;

/**
 * <p> The DTN protocol can be extended by introducing new ExtensionBlock or new EID scheme.
 * Introducing a new Block thus requires the following:
 *
 * <ul>
 *     <li>Being able to instantiate a new block given its type</li>
 *     <li>Being able to parse the block data specific part</li>
 *     <li>Being able to serialize the block data specific part</li>
 *     <li>Being able to process the block during the bundle lifecycle</li>
 * </ul>
 *
 * The ExtensionToolbox provides an easy access to all of the above. It is intended to be used
 * by modules or core component. Similarly, introducing a new EID scheme requires the same
 * ability to instantiate a new EID based on the scheme specific part. ExtensionToolbox also
 * provides an easy access to the EID factory.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public interface ExtensionToolbox {

    /**
     * get the block factory to instantiate a new Block.
     *
     * @return BlockFactory
     */
    BlockFactory getBlockFactory();

    /**
     * get the block-specific data parser factory.
     *
     * @return BlockDataParserFactory
     */
    BlockDataParserFactory getBlockDataParserFactory();

    /**
     * get the block-specific data serializer factory.
     *
     * @return BlockDataSerializerFactory
     */
    BlockDataSerializerFactory getBlockDataSerializerFactory();

    /**
     * get the block-specific processor factory.
     *
     * @return BlockProcessorFactory
     */
    BlockProcessorFactory getBlockProcessorFactory();

    /**
     * get the EID factory
     *
     * @return EIDFactory
     */
    EIDFactory getEIDFactory();

}
