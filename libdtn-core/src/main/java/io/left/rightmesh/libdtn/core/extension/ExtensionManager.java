package io.left.rightmesh.libdtn.core.extension;

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
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;
import io.left.rightmesh.libdtn.common.data.eid.UnknownClaEid;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerApi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ExtensionManager implements the ExtensionManagerAPI and provides entry point to add new
 * extension blocks and eids into the core.
 *
 * @author Lucien Loiseau on 22/11/18.
 */
public class ExtensionManager implements ExtensionManagerApi {

    public static final String TAG = "ExtensionManager";

    /* extension block */
    private Map<Integer, Supplier<CanonicalBlock>> extensionBlockFactory = new HashMap<>();
    private Map<Integer, Supplier<CborParser>> extensionBlockParserFactory = new HashMap<>();
    private Map<Integer, Supplier<CborEncoder>> extensionBlockSerializerFactory = new HashMap<>();
    private Map<Integer, Supplier<BlockProcessor>> extensionBlockProcessorFactory = new HashMap<>();

    /* extension Eid */
    private Map<Integer, String> extensionEidSchemeIana = new HashMap<>();
    private Map<String, EidSspParser> extensionEidParser = new HashMap<>();
    private Map<String, ClaEidParser> extensionClaEidParser = new HashMap<>();

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
        public CborParser create(int type,
                                 CanonicalBlock block,
                                 BlobFactory blobFactory,
                                 EidFactory eidFactory,
                                 Log logger) throws UnknownBlockTypeException {
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
                if (extensionEidSchemeIana.containsKey(ianaScheme)) {
                    return extensionEidSchemeIana.get(ianaScheme);
                }
            }
            throw new UnknownIanaNumber(ianaScheme);
        }

        @Override
        public Eid create(String scheme, String ssp) throws EidFormatException {
            try {
                return super.create(scheme, ssp);
            } catch (UnknownEidScheme ues) {
                if (extensionEidParser.containsKey(scheme)) {
                    return extensionEidParser.get(scheme).create(ssp);
                }
            }
            throw new UnknownEidScheme(scheme);
        }

        @Override
        public ClaEid create(String claName, String claSpecific, String claSink)
                throws EidFormatException {
            try {
                return super.create(claName, claSpecific, claSink);
            } catch (UnknownClaName ucn) {
                if (extensionClaEidParser.containsKey(claName)) {
                    return extensionClaEidParser.get(claName).create(claName, claSpecific, claSink);
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
                                  Supplier<BlockProcessor> processor)
            throws BlockTypeAlreadyManaged {
        if (extensionBlockFactory.containsKey(type)) {
            throw new BlockTypeAlreadyManaged();
        }
        extensionBlockFactory.put(type, block);
        extensionBlockParserFactory.put(type, parser);
        extensionBlockSerializerFactory.put(type, serializer);
        extensionBlockProcessorFactory.put(type, processor);
    }

    @Override
    public void addExtensionEid(int ianaNumber, String scheme, EidSspParser parser)
            throws EidSchemeAlreadyManaged {
        if (extensionEidParser.containsKey(scheme)) {
            throw new EidSchemeAlreadyManaged();
        }
        extensionEidSchemeIana.put(ianaNumber, scheme);
        extensionEidParser.put(scheme, parser);
        logger.v(TAG, "new Eid added: " + scheme + " (iana = " + ianaNumber + ")");
    }

    @Override
    public void addExtensionCla(String clName, ClaEidParser parser) throws ClaNameAlreadyManaged {
        if (extensionClaEidParser.containsKey(clName)) {
            throw new ClaNameAlreadyManaged();
        }
        extensionClaEidParser.put(clName, parser);
        logger.v(TAG, "new CLA added: cla:" + clName);
    }
}
