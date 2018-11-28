package io.left.rightmesh.libdtn.core;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.CLAEIDParser;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDSspParser;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerAPI;
import io.left.rightmesh.libdtn.core.api.BundleProcessorAPI;
import io.left.rightmesh.libdtn.core.api.CLAManagerAPI;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.DeliveryAPI;
import io.left.rightmesh.libdtn.core.api.LinkLocalRoutingAPI;
import io.left.rightmesh.libdtn.core.api.LocalEIDAPI;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.api.RoutingAPI;
import io.left.rightmesh.libdtn.core.api.RoutingTableAPI;
import io.left.rightmesh.libdtn.core.api.StorageAPI;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public class MockCore implements CoreAPI  {
    @Override
    public ConfigurationAPI getConf() {
        return null;
    }

    @Override
    public Log getLogger() {
        return new SimpleLogger();
    }

    @Override
    public LocalEIDAPI getLocalEID() {
        return null;
    }

    @Override
    public ExtensionManagerAPI getExtensionManager() {
        return new ExtensionManagerAPI() {
            @Override
            public EIDFactory getEIDFactory() {
                return new BaseEIDFactory();
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
            public void addExtensionCLA(String cl_name, CLAEIDParser parser) {
            }

            @Override
            public void addExtensionEID(int schemeId, String schemeStr, EIDSspParser ssPparser) {
            }
        };
    }

    @Override
    public RoutingAPI getRoutingEngine() {
        return null;
    }

    @Override
    public RegistrarAPI getRegistrar() {
        return null;
    }

    @Override
    public DeliveryAPI getDelivery() {
        return null;
    }

    @Override
    public BundleProcessorAPI getBundleProcessor() {
        return null;
    }

    @Override
    public StorageAPI getStorage() {
        return null;
    }

    @Override
    public CLAManagerAPI getClaManager() {
        return null;
    }

    @Override
    public LinkLocalRoutingAPI getLinkLocalRouting() {
        return null;
    }

    @Override
    public RoutingTableAPI getRoutingTable() {
        return null;
    }

    @Override
    public ModuleLoaderAPI getModuleLoader() {
        return null;
    }
}
