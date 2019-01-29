package io.left.rightmesh.libdtn.core.storage;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Test class for SimpleStorage.
 * @author Lucien Loiseau on 04/10/18.
 */
public class SimpleStorageTest {

    public static final AtomicReference<CountDownLatch> WAIT_LOCK = new AtomicReference<>(new CountDownLatch(1));
    private Set<String> paths = new HashSet<>();
    private CoreConfiguration conf = new CoreConfiguration();
    private Storage storage;
    private CoreApi mockCore = mockCore();

    /* mocking the core */
    private CoreApi mockCore() {
        return new MockCore() {
            @Override
            public ConfigurationApi getConf() {
                conf.<Boolean>get(COMPONENT_ENABLE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(false);
                conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(true);

                Set<String> paths = new HashSet<>();
                paths.add("/tmp/");
                conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
                return conf;
            }

            @Override
            public ExtensionManagerApi getExtensionManager() {
                return new MockExtensionManager() {
                    @Override
                    public BlockDataParserFactory getBlockDataParserFactory() {
                        return new BaseBlockDataParserFactory();
                    }

                    @Override
                    public BlockFactory getBlockFactory() {
                        return new BaseBlockFactory();
                    }

                    @Override
                    public EidFactory getEidFactory() {
                        return new BaseEidFactory();
                    }

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
    public void testSimpleStoreBundle() {
        synchronized (StorageTest.LOCK) {
            File dir = new File("/tmp/bundle/");
            System.out.println("[+] SimpleStorage");
            storage = new Storage(mockCore);
            storage.initComponent(mockCore.getConf(), COMPONENT_ENABLE_STORAGE, mockCore.getLogger());

            System.out.println("[.] clear SimpleStorage");
            clearStorage();
            assertStorageSize(0);
            assertFileStorageSize(0, dir);

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            /* store the bundles in storage */
            System.out.println("[.] store in SimpleStorage");
            cockLock();
            Observable.fromArray(bundles).flatMapCompletable(
                    b -> Completable.fromSingle(storage.getSimpleStorage().store(b)))
                    .subscribe(
                            () -> {
                                WAIT_LOCK.get().countDown();
                            },
                            e -> {
                                WAIT_LOCK.get().countDown();
                            });
            waitFinish();
            assertStorageSize(bundles.length);
            assertFileStorageSize(bundles.length, dir);

            /* pull the bundles from storage  */
            System.out.println("[.] pull from SimpleStorage");
            final LinkedList<Bundle> pulledBundles = new LinkedList<>();
            cockLock();
            Observable.fromArray(bundles).flatMapCompletable(
                    b -> Completable.create(s ->
                            storage.getSimpleStorage().get(b.bid).subscribe(
                                    pb -> {
                                        pulledBundles.add(pb);
                                        s.onComplete();
                                    },
                                    e -> {
                                        e.printStackTrace();
                                        System.out.println("error pulling bundle: " + e.getMessage());
                                        s.onComplete();
                                    })))
                    .subscribe(
                            () -> WAIT_LOCK.get().countDown(),
                            e -> WAIT_LOCK.get().countDown());
            waitFinish();
            assertEquals(bundles.length, pulledBundles.size());
            assertFileStorageSize(bundles.length, dir);

            /* check that they are the same */
            for (Bundle bundle : pulledBundles) {
                boolean found = false;
                for (int j = 0; j < bundles.length; j++) {
                    if (bundles[j].bid.getBidString().equals(bundle.bid.getBidString())) {
                        found = true;
                        assertArrayEquals(
                                flowableToByteArray(bundles[j].getPayloadBlock().data.observe()),
                                flowableToByteArray(bundle.getPayloadBlock().data.observe()));
                        bundle.clearBundle();
                    }
                }
                assertTrue(found);
            }

            /* check remove path */
            System.out.println("[.] remove path from SimpleStorage configuration");
            paths.clear();
            conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
            try {
                // give it time to unindex
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                // ignore
            }
            assertStorageSize(0);
            assertFileStorageSize(6, dir);

            /* check indexing new path */
            System.out.println("[.] add path to SimpleStorage configuration for indexing");
            paths.add("/tmp/");
            conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
            try {
                // give it time to index
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                // ignore
            }
            assertStorageSize(6);
            assertFileStorageSize(6, dir);

            /* clear the storage */
            System.out.println("[.] clear SimpleStorage");
            clearStorage();
            assertStorageSize(0);
            assertFileStorageSize(0, dir);

            for(Bundle bundle : bundles) {
                bundle.clearBundle();
            }
        }
    }


    public void cockLock() {
        WAIT_LOCK.set(new CountDownLatch(1));
    }

    public void waitFinish() {
        try {
            WAIT_LOCK.get().await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    private byte[] flowableToByteArray(Flowable<ByteBuffer> f) {
        AtomicInteger size = new AtomicInteger();
        f.subscribe(b -> size.addAndGet(b.remaining()));
        ByteBuffer ret = ByteBuffer.allocate(size.get());
        f.subscribe(ret::put);
        return ret.array();
    }

    private void clearStorage() {
        cockLock();
        storage.clear().subscribe(
                () -> WAIT_LOCK.get().countDown(),
                e -> WAIT_LOCK.get().countDown()
        );
        waitFinish();
    }

    void assertStorageSize(int expectedSize) {
        assertEquals(expectedSize, storage.getSimpleStorage().count());
    }

    void assertFileStorageSize(int expectedSize, File dir) {
        assertEquals(expectedSize, dir.listFiles().length);
    }


}
