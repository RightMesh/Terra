package io.left.rightmesh.libdtn.core.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BaseBlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;
import io.left.rightmesh.libdtn.common.data.eid.UnknownClaEid;
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

    /* extension Eid */
    private Map<Integer, String> extensionEIDSchemeIana = new HashMap<>();
    private Map<String, EidSspParser> extensionEIDParser = new HashMap<>();
    private Map<String, ClaEidParser> extensionCLAEIDParser = new HashMap<>();

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
        public CborParser create(int type, CanonicalBlock block, BlobFactory blobFactory, EidFactory eidFactory, Log logger) throws UnknownBlockTypeException {
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

    private EidFactory eidFactory = new BaseEidFactory(true) {
        @Override
        public String getIanaScheme(int ianaScheme) throws UnknownIanaNumber {
            try {
                return super.getIanaScheme(ianaScheme);
            } catch (UnknownIanaNumber uin) {
                if (extensionEIDSchemeIana.containsKey(ianaScheme)) {
                    return extensionEIDSchemeIana.get(ianaScheme);
                }
            }
            throw new UnknownIanaNumber(ianaScheme);
        }

        @Override
        public Eid create(String scheme, String ssp) throws EidFormatException {
            try {
                return super.create(scheme, ssp);
            } catch (UnknownEidScheme ues) {
                if (extensionEIDParser.containsKey(scheme)) {
                    return extensionEIDParser.get(scheme).create(ssp);
                }
            }
            throw new UnknownEidScheme(scheme);
        }

        @Override
        public ClaEid create(String claName, String claSpecific, String claSink) throws EidFormatException {
            try {
                return super.create(claName, claSpecific, claSink);
            } catch (UnknownClaName ucn) {
                if (extensionCLAEIDParser.containsKey(claName)) {
                    return extensionCLAEIDParser.get(claName).create(claName, claSpecific, claSink);
                } else {
                    return new UnknownClaEid(claName, claSpecific, claSink);
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
    public EidFactory getEidFactory() {
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
    public void addExtensionEID(int iana_number, String scheme, EidSspParser parser) throws EIDSchemeAlreadyManaged {
        if (extensionEIDParser.containsKey(scheme)) {
            throw new EIDSchemeAlreadyManaged();
        }
        extensionEIDSchemeIana.put(iana_number, scheme);
        extensionEIDParser.put(scheme, parser);
        logger.v(TAG, "new Eid added: " + scheme + " (iana = " + iana_number + ")");
    }

    @Override
    public void addExtensionCLA(String cl_name, ClaEidParser parser) throws CLNameAlreadyManaged {
        if (extensionCLAEIDParser.containsKey(cl_name)) {
            throw new CLNameAlreadyManaged();
        }
        extensionCLAEIDParser.put(cl_name, parser);
        logger.v(TAG, "new CLA added: cla:" + cl_name);
    }
}
