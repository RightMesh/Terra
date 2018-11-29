package io.left.rightmesh.libdtn.core.extension;

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
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.CLAEID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.CLAEIDParser;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EIDSspParser;
import io.left.rightmesh.libdtn.common.data.eid.UnknownCLAEID;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerAPI;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public class ExtensionManager implements ExtensionManagerAPI {

    public static final String TAG = "ExtensionManager";

    /* extension block */
    private Map<Integer, Supplier<CanonicalBlock>> extensionBlockFactory = new HashMap<>();
    private Map<Integer, Supplier<CborParser>> extensionBlockParserFactory = new HashMap<>();
    private Map<Integer, Supplier<CborEncoder>> extensionBlockSerializerFactory = new HashMap<>();
    private Map<Integer, Supplier<BlockProcessor>> extensionBlockProcessorFactory = new HashMap<>();

    /* extension EID */
    private Map<Integer, String> extensionEIDSchemeIana = new HashMap<>();
    private Map<String, EIDSspParser> extensionEIDParser = new HashMap<>();
    private Map<String, CLAEIDParser> extensionCLAEIDParser = new HashMap<>();

    private Log logger;

    public ExtensionManager(Log logger) {
        this.logger = logger;
    }

    private BlockFactory coreBlockFactory = new BlockFactory() {
        BlockFactory baseBlockFactory = new BaseBlockFactory();

        @Override
        public CanonicalBlock create(int type) throws UnknownBlockTypeException {
            try {
                return baseBlockFactory.create(type);
            } catch (UnknownBlockTypeException ubte) {
                if (extensionBlockFactory.containsKey(type)) {
                    return extensionBlockFactory.get(type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    private BlockDataParserFactory coreBlockParserFactory = new BlockDataParserFactory() {
        BaseBlockDataParserFactory baseBlockParserFactory = new BaseBlockDataParserFactory();

        @Override
        public CborParser create(int type, CanonicalBlock block, BLOBFactory blobFactory, EIDFactory eidFactory, Log logger) throws UnknownBlockTypeException {
            try {
                return baseBlockParserFactory.create(type, block, blobFactory, eidFactory, logger);
            } catch (UnknownBlockTypeException ubte) {
                if (extensionBlockFactory.containsKey(type)) {
                    return extensionBlockParserFactory.get(type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    private BlockDataSerializerFactory coreSerializerFactory = new BlockDataSerializerFactory() {
        BaseBlockDataSerializerFactory baseSerializerFactory = new BaseBlockDataSerializerFactory();

        @Override
        public CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException {
            try {
                return baseSerializerFactory.create(block);
            } catch (UnknownBlockTypeException ubte) {
                if (extensionBlockSerializerFactory.containsKey(block.type)) {
                    return extensionBlockSerializerFactory.get(block.type).get();
                }
            }
            throw new UnknownBlockTypeException();
        }
    };

    private BlockProcessorFactory coreProcessorFactory = new BlockProcessorFactory() {
        BaseBlockProcessorFactory baseBlockProcessorFactory = new BaseBlockProcessorFactory();

        @Override
        public BlockProcessor create(int type) throws ProcessorNotFoundException {
            try {
                return baseBlockProcessorFactory.create(type);
            } catch (ProcessorNotFoundException pnfe) {
                if (extensionBlockProcessorFactory.containsKey(type)) {
                    return extensionBlockProcessorFactory.get(type).get();
                }
            }
            throw new ProcessorNotFoundException();
        }
    };

    private EIDFactory eidFactory = new BaseEIDFactory(true) {
        @Override
        public String getIANAScheme(int iana_scheme) throws UnknownIanaNumber {
            try {
                return super.getIANAScheme(iana_scheme);
            } catch (UnknownIanaNumber uin) {
                if (extensionEIDSchemeIana.containsKey(iana_scheme)) {
                    return extensionEIDSchemeIana.get(iana_scheme);
                }
            }
            throw new UnknownIanaNumber(iana_scheme);
        }

        @Override
        public EID create(String scheme, String ssp) throws EIDFormatException {
            try {
                return super.create(scheme, ssp);
            } catch (UnknownEIDScheme ues) {
                if (extensionEIDParser.containsKey(scheme)) {
                    return extensionEIDParser.get(scheme).create(ssp);
                }
            }
            throw new UnknownEIDScheme(scheme);
        }

        @Override
        public CLAEID create(String cl_name, String cl_ssp, String cl_sink) throws EIDFormatException {
            try {
                return super.create(cl_name, cl_ssp, cl_sink);
            } catch (UnknownCLName ucn) {
                if (extensionCLAEIDParser.containsKey(cl_name)) {
                    return extensionCLAEIDParser.get(cl_name).create(cl_name, cl_ssp, cl_sink);
                } else {
                    return new UnknownCLAEID(cl_name, cl_ssp, cl_sink);
                }
            }
        }
    };

    @Override
    public BlockFactory getBlockFactory() {
        return coreBlockFactory;
    }

    @Override
    public BlockDataParserFactory getBlockDataParserFactory() {
        return coreBlockParserFactory;
    }

    @Override
    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
        return coreSerializerFactory;
    }

    @Override
    public EIDFactory getEIDFactory() {
        return eidFactory;
    }

    @Override
    public BlockProcessorFactory getBlockProcessorFactory() {
        return coreProcessorFactory;
    }

    @Override
    public void addExtensionBlock(int type,
                                  Supplier<CanonicalBlock> block,
                                  Supplier<CborParser> parser,
                                  Supplier<CborEncoder> serializer,
                                  Supplier<BlockProcessor> processor) throws BlockTypeAlreadyManaged {
        if (extensionBlockFactory.containsKey(type)) {
            throw new BlockTypeAlreadyManaged();
        }
        extensionBlockFactory.put(type, block);
        extensionBlockParserFactory.put(type, parser);
        extensionBlockSerializerFactory.put(type, serializer);
        extensionBlockProcessorFactory.put(type, processor);
    }

    @Override
    public void addExtensionEID(int iana_number, String scheme, EIDSspParser parser) throws EIDSchemeAlreadyManaged {
        if (extensionEIDParser.containsKey(scheme)) {
            throw new EIDSchemeAlreadyManaged();
        }
        extensionEIDSchemeIana.put(iana_number, scheme);
        extensionEIDParser.put(scheme, parser);
        logger.v(TAG, "new EID added: " + scheme + " (iana = " + iana_number + ")");
    }

    @Override
    public void addExtensionCLA(String cl_name, CLAEIDParser parser) throws CLNameAlreadyManaged {
        if (extensionCLAEIDParser.containsKey(cl_name)) {
            throw new CLNameAlreadyManaged();
        }
        extensionCLAEIDParser.put(cl_name, parser);
        logger.v(TAG, "new CLA added: cla:" + cl_name);
    }
}
