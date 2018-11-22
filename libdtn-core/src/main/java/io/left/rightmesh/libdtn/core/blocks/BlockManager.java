package io.left.rightmesh.libdtn.core.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BaseBlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.BlockManagerAPI;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public class BlockManager implements BlockManagerAPI {

    private Map<Integer, Supplier<CanonicalBlock>> extensionBlockFactory = new HashMap<>();
    private Map<Integer, Supplier<CborParser>> extensionBlockParserFactory = new HashMap<>();
    private Map<Integer, Supplier<CborEncoder>> extensionBlockSerializerFactory = new HashMap<>();

    private BlockFactory coreBlockFactory = new BlockFactory() {
        BlockFactory baseBlockFactory = new BaseBlockFactory();
        @Override
        public CanonicalBlock create(int type) throws UnknownBlockTypeException {
            try {
                return baseBlockFactory.create(type);
            } catch(UnknownBlockTypeException ubte) {
                if(extensionBlockFactory.containsKey(type)) {
                    return extensionBlockFactory.get(type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    private BlockDataParserFactory coreParserFactory = new BlockDataParserFactory() {
        BaseBlockDataParserFactory baseParserFactory = new BaseBlockDataParserFactory();
        @Override
        public CborParser create(int type, CanonicalBlock block, BLOBFactory blobFactory, Log logger) throws UnknownBlockTypeException {
            try {
                return baseParserFactory.create(type, block, blobFactory, logger);
            } catch(UnknownBlockTypeException ubte) {
                if(extensionBlockFactory.containsKey(type)) {
                    return extensionBlockParserFactory.get(type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    private BlockDataSerializerFactory coreSerializerFactory= new BlockDataSerializerFactory() {
        BaseBlockDataSerializerFactory baseSerializerFactory = new BaseBlockDataSerializerFactory();

        @Override
        public CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException {
            try {
                return baseSerializerFactory.create(block);
            } catch(UnknownBlockTypeException ubte) {
                if(extensionBlockSerializerFactory.containsKey(block.type)) {
                    return extensionBlockSerializerFactory.get(block.type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    @Override
    public BlockFactory getBlockFactory() {
        return coreBlockFactory;
    }

    @Override
    public BlockDataParserFactory getBlockDataParserFactory() {
        return coreParserFactory;
    }

    @Override
    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
        return coreSerializerFactory;
    }

    @Override
    public void addExtensionBlock(int type,
                           Supplier<CanonicalBlock> block,
                           Supplier<CborParser> parser,
                           Supplier<CborEncoder> serializer) throws BlockTypeAlreadyManaged {
        if(extensionBlockFactory.containsKey(type)) {
            throw new BlockTypeAlreadyManaged();
        }
        extensionBlockFactory.put(type, block);
        extensionBlockParserFactory.put(type, parser);
        extensionBlockSerializerFactory.put(type, serializer);
    }
}
