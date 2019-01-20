package io.left.rightmesh.libdtn.core.storage;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertEquals;

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
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Test class for Storage.
 *
 * @author Lucien Loiseau on 14/10/18.
 */
public class StorageTest {

    static final Object LOCK = new Object();
    static final AtomicReference<CountDownLatch> WAIT_LOCK
            = new AtomicReference<>(new CountDownLatch(1));
    private File dir = new File(System.getProperty("path") + "/bundle/");
    private Storage storage;
    private CoreApi mockCore = mockCore();

    /* mocking the core */
    public CoreApi mockCore() {
        return new MockCore() {
            @Override
            public ConfigurationApi getConf() {
                CoreConfiguration conf = new CoreConfiguration();
                conf.<Boolean>get(COMPONENT_ENABLE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(true);

                Set<String> paths = new HashSet<>();
                paths.add(System.getProperty("path"));
                conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
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
    public void testStoreBundleBothStorage() {
        synchronized (LOCK) {
            System.out.println("[+] Meta Storage ");

            storage = new Storage(mockCore());
            storage.initComponent(
                    mockCore.getConf(),
                    COMPONENT_ENABLE_STORAGE,
                    mockCore.getLogger());

            System.out.println("[.] clear Storage");
            cockLock();
            storage.clear().subscribe(
                    () -> WAIT_LOCK.get().countDown(),
                    e -> WAIT_LOCK.get().countDown()
            );
            waitFinish();

            assertEquals(0, storage.count());
            assertEquals(0, storage.getVolatileStorage().count());
            assertEquals(0, storage.getSimpleStorage().count());
            assertFileStorageSize(0, dir);

            System.out.println("[.] store bundle in Storage");

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            cockLock();
            Observable.fromArray(bundles).flatMapCompletable(
                    b -> Completable.fromSingle(storage.store(b)))
                    .subscribe(
                            () -> WAIT_LOCK.get().countDown(),
                            e -> WAIT_LOCK.get().countDown());
            waitFinish();

            assertEquals(bundles.length, storage.count());
            assertEquals(bundles.length, storage.getVolatileStorage().count());
            assertEquals(bundles.length, storage.getSimpleStorage().count());
            assertFileStorageSize(bundles.length, dir);

            System.out.println("[.] clear Storage");
            cockLock();
            storage.clear().subscribe(
                    () -> WAIT_LOCK.get().countDown(),
                    e -> WAIT_LOCK.get().countDown()
            );
            waitFinish();

            assertEquals(0, storage.count());
            assertEquals(0, storage.getVolatileStorage().count());
            assertEquals(0, storage.getSimpleStorage().count());
            assertFileStorageSize(0, dir);

            for(Bundle bundle : bundles) {
                bundle.clearBundle();
            }
        }
    }

    static void cockLock() {
        WAIT_LOCK.set(new CountDownLatch(1));
    }

    static void waitFinish() {
        try {
            WAIT_LOCK.get().await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
    }


    void assertStorageSize(int expectedSize) {
        assertEquals(expectedSize, storage.getSimpleStorage().count());
    }

    void assertFileStorageSize(int expectedSize, File dir) {
        assertEquals(expectedSize, dir.listFiles().length);
    }


}
