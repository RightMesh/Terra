package io.left.rightmesh.libdtn.core.storage;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BaseBlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;
import io.left.rightmesh.libdtn.core.CoreConfiguration;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.core.MockExtensionManager;
import io.left.rightmesh.libdtn.core.MockCore;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerApi;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;

import org.junit.Test;

/**
 * Test class for VolatileStorage.
 *
 * @author Lucien Loiseau on 27/09/18.
 */
public class VolatileStorageTest {

    private CoreApi mockCore = mockCore();

    /* mocking the core */
    public CoreApi mockCore() {
        return new MockCore() {
            @Override
            public ConfigurationApi getConf() {
                CoreConfiguration conf = new CoreConfiguration();
                conf.<Boolean>get(COMPONENT_ENABLE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(false);
                return conf;
            }

            @Override
            public ExtensionManagerApi getExtensionManager() {
                return new MockExtensionManager() {
                    @Override
                    public BlockDataSerializerFactory getBlockDataSerializerFactory() {
                        return new BaseBlockDataSerializerFactory();
                    }

                    @Override
                    public BlockProcessorFactory getBlockProcessorFactory() {
                        return new BaseBlockProcessorFactory();
                    }
                };
            }

            @Override
            public Log getLogger() {
                return new SimpleLogger();
            }
        };
    }

    @Test
    public void testVolatileStoreBundle() {
        synchronized (StorageTest.LOCK) {
            System.out.println("[+] Volatile Storage");
            Storage storage = new Storage(mockCore);
            storage.initComponent(
                    mockCore.getConf(),
                    COMPONENT_ENABLE_STORAGE,
                    mockCore.getLogger());


            System.out.println("[.] clear VolatileStorage");
            storage.getVolatileStorage().clear().subscribe();
            assertEquals(0, storage.getVolatileStorage().count());

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            System.out.println("[.] store bundle in VolatileStorage");
            for (int i = 0; i < bundles.length; i++) {
                final int j = i;
                storage.getVolatileStorage().store(bundles[j]).subscribe(
                        (b) -> {
                            assertEquals(j + 1, storage.getVolatileStorage().count());
                            assertEquals(true, storage.containsVolatile(b.bid));
                        },
                        e -> fail());
            }
            assertEquals(bundles.length, storage.getVolatileStorage().count());

            System.out.println("[.] clear VolatileStorage");
            storage.getVolatileStorage().clear().subscribe();
            assertEquals(0, storage.getVolatileStorage().count());
        }
    }

}
