package io.left.rightmesh.libdtn.core;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;
import io.left.rightmesh.libdtn.core.api.BundleProtocolApi;
import io.left.rightmesh.libdtn.core.api.ClaManagerApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.DeliveryApi;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerApi;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.LinkLocalTableApi;
import io.left.rightmesh.libdtn.core.api.LocalEidApi;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderApi;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;
import io.left.rightmesh.libdtn.core.api.RoutingEngineApi;
import io.left.rightmesh.libdtn.core.api.RoutingTableApi;
import io.left.rightmesh.libdtn.core.api.StorageApi;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public class MockCore implements CoreApi {
    @Override
    public void init() {
    }

    @Override
    public ConfigurationApi getConf() {
        return null;
    }

    @Override
    public Log getLogger() {
        return new SimpleLogger();
    }

    @Override
    public LocalEidApi getLocalEid() {
        return null;
    }

    @Override
    public ExtensionManagerApi getExtensionManager() {
        return new ExtensionManagerApi() {
            @Override
            public EidFactory getEidFactory() {
                return new BaseEidFactory();
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
                return new BaseBlockProcessorFactory();
            }

            @Override
            public void addExtensionBlock(int type, Supplier<CanonicalBlock> block, Supplier<CborParser> parser, Supplier<CborEncoder> serializer, Supplier<BlockProcessor> processor) throws BlockTypeAlreadyManaged {
            }

            @Override
            public void addExtensionClaEid(String clName, ClaEidParser parser) {
            }

            @Override
            public void addExtensionEid(int schemeId, String schemeStr, EidSspParser ssPparser) {
            }
        };
    }

    @Override
    public RoutingEngineApi getRoutingEngine() {
        return null;
    }

    @Override
    public RegistrarApi getRegistrar() {
        return null;
    }

    @Override
    public DeliveryApi getDelivery() {
        return null;
    }

    @Override
    public BundleProtocolApi getBundleProtocol() {
        return null;
    }

    @Override
    public StorageApi getStorage() {
        return null;
    }

    @Override
    public ClaManagerApi getClaManager() {
        return null;
    }

    @Override
    public LinkLocalTableApi getLinkLocalTable() {
        return null;
    }

    @Override
    public RoutingTableApi getRoutingTable() {
        return null;
    }

    @Override
    public ModuleLoaderApi getModuleLoader() {
        return null;
    }
}
