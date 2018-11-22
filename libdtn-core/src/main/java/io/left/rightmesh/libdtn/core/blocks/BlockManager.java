package io.left.rightmesh.libdtn.core.blocks;

import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BaseBlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.core.api.BlockManagerAPI;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public class BlockManager implements BlockManagerAPI {

    BlockFactory blockFactory;
    BlockDataParserFactory parserFactory;
    BlockDataSerializerFactory serializerFactory;

    public BlockManager() {
        blockFactory = new BaseBlockFactory();
        parserFactory = new BaseBlockDataParserFactory();
        serializerFactory = new BaseBlockDataSerializerFactory();
    }

    @Override
    public BlockFactory getBlockFactory() {
        return blockFactory;
    }

    @Override
    public BlockDataParserFactory getBlockDataParserFactory() {
        return parserFactory;
    }

    @Override
    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
        return serializerFactory;
    }
}
