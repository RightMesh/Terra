package io.left.rightmesh.libdtn.core.api;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public interface BlockManagerAPI {

    class BlockTypeAlreadyManaged extends Exception {
    }


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

    /**
     * Add a new ExtensionBlock.
     *
     * @param type block type
     * @param block block supplier
     * @param parser parser supplier
     * @param serializer serializer supplier
     * @throws BlockTypeAlreadyManaged if the block is already managed
     */
    void addExtensionBlock(int type,
                           Supplier<CanonicalBlock> block,
                           Supplier<CborParser> parser,
                           Supplier<CborEncoder> serializer) throws BlockTypeAlreadyManaged;

}
