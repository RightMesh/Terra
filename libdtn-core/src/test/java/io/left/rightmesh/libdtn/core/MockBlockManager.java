package io.left.rightmesh.libdtn.core;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.core.api.BlockManagerAPI;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public class MockBlockManager implements BlockManagerAPI  {
    @Override
    public BlockFactory getBlockFactory() {
        return null;
    }

    @Override
    public BlockDataParserFactory getBlockDataParserFactory() {
        return null;
    }

    @Override
    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
        return null;
    }

    @Override
    public BlockProcessorFactory getBlockProcessorFactory() {
        return null;
    }

    @Override
    public void addExtensionBlock(int type, Supplier<CanonicalBlock> block, Supplier<CborParser> parser, Supplier<CborEncoder> serializer, Supplier<BlockProcessor> processor) throws BlockTypeAlreadyManaged {

    }
}
