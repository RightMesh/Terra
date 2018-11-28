package io.left.rightmesh.libdtn.common;

import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BaseBlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class BaseExtensionToolbox implements ExtensionToolbox {

    @Override
    public EIDFactory getEIDFactory() {
        return new BaseEIDFactory();
    }

    @Override
    public BlockFactory getBlockFactory() {
        return new BaseBlockFactory();
    }

    @Override
    public BlockDataParserFactory getBlockDataParserFactory() {
        return new BaseBlockDataParserFactory();
    }

    @Override
    public BlockProcessorFactory getBlockProcessorFactory() {
        return new BaseBlockProcessorFactory();
    }

    @Override
    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
        return new BaseBlockDataSerializerFactory();
    }

}
