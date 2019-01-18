package io.left.rightmesh.libdtn.common;

import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BaseBlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;

/**
 * BaseExtensionToolbox implements the ExtensionToolbox ApiEid and provide
 * factory for all the base {@link Block} and {@link Eid} classes.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public class BaseExtensionToolbox implements ExtensionToolbox {

    @Override
    public EidFactory getEidFactory() {
        return new BaseEidFactory();
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
