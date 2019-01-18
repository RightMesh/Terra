package io.left.rightmesh.libdtn.core;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerAPI;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public class MockExtensionManager implements ExtensionManagerAPI {
    @Override
    public EidFactory getEidFactory() {
        return null;
    }

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
    public void addExtensionBlock(int type,
                                  Supplier<CanonicalBlock> block,
                                  Supplier<CborParser> parser,
                                  Supplier<CborEncoder> serializer,
                                  Supplier<BlockProcessor> processor) throws BlockTypeAlreadyManaged {
    }

    @Override
    public void addExtensionCLA(String cl_name, ClaEidParser parser) {
    }

    @Override
    public void addExtensionEID(int schemeId, String schemeStr, EidSspParser ssPparser) {
    }
}
