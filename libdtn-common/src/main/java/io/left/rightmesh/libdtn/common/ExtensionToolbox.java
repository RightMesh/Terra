package io.left.rightmesh.libdtn.common;

import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;

/**
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
