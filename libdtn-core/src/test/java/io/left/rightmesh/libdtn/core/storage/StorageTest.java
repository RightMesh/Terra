package io.left.rightmesh.libdtn.core.storage;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class StorageTest {

    public static final Object lock = new Object();
    public static final AtomicReference<CountDownLatch> waitLock = new AtomicReference<>(new CountDownLatch(1));
    private Storage storage;

    public void testStoreBundleBothStorage() {
        synchronized (lock) {
            System.out.println("[+] Meta Storage ");
            DTNConfiguration conf = new DTNConfiguration();
            conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
            conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(true);
            Set<String> paths = new HashSet<>();
            paths.add(System.getProperty("path"));
            File dir = new File(System.getProperty("path") + "/bundle/");
            conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
            storage = new Storage(conf, new Logger(conf));

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            System.out.println("[.] clear Storage");
            cockLock();
            storage.clear().subscribe(
                    () -> waitLock.get().countDown(),
                    e -> waitLock.get().countDown()
            );
            waitFinish();

            assertEquals(0, storage.count());
            assertEquals(0, storage.getVolatileStorage().count());
            assertEquals(0, storage.getSimpleStorage().count());
            assertFileStorageSize(0, dir);

            System.out.println("[.] store bundle in Storage");

            cockLock();
            Observable.fromArray(bundles).flatMapCompletable(
                    b -> Completable.fromSingle(storage.store(b)))
                    .subscribe(
                            () -> waitLock.get().countDown(),
                            e -> waitLock.get().countDown());
            waitFinish();

            assertEquals(bundles.length, storage.count());
            assertEquals(bundles.length, storage.getVolatileStorage().count());
            assertEquals(bundles.length, storage.getSimpleStorage().count());
            assertFileStorageSize(bundles.length, dir);

            System.out.println("[.] clear Storage");
            cockLock();
            storage.clear().subscribe(
                    () -> waitLock.get().countDown(),
                    e -> waitLock.get().countDown()
            );
            waitFinish();

            assertEquals(0, storage.count());
            assertEquals(0, storage.getVolatileStorage().count());
            assertEquals(0, storage.getSimpleStorage().count());
            assertFileStorageSize(0, dir);
        }
    }

    public static void cockLock() {
        waitLock.set(new CountDownLatch(1));
    }

    public static void waitFinish() {
        try {
            waitLock.get().await(2000, TimeUnit.MILLISECONDS);
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
