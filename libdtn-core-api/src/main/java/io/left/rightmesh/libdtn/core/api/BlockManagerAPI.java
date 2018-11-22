package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public interface BlockManagerAPI {

    /**
     * get the block factory to instantiate new Block.
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

}
