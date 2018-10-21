package io.left.rightmesh.libdtn.storage;

import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.common.data.Bundle;

import io.left.rightmesh.libdtn.storage.bundle.SimpleStorage;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Lucien Loiseau on 04/10/18.
 */
public class SimpleStorageTest {

    public static final AtomicReference<CountDownLatch> waitLock = new AtomicReference<>(new CountDownLatch(1));

    @Test
    public void testSimpleStoreBundle() {
        synchronized (StorageTest.lock) {
            System.out.println("[+] SimpleStorage");
            Set<String> paths = new HashSet<>();
            DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(false);
            DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(true);
            paths.add(System.getProperty("path"));
            File dir = new File(System.getProperty("path") + "/bundle/");
            DTNConfiguration.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
            SimpleStorage.getInstance();

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            System.out.println("[.] clear SimpleStorage");
            clearStorage();
            assertStorageSize(0);
            assertFileStorageSize(0, dir);


            /* store the bundles in storage */
            System.out.println("[.] store in SimpleStorage");
            cockLock();
            Observable.fromArray(bundles).flatMapCompletable(
                    b -> Completable.fromSingle(SimpleStorage.store(b)))
                    .subscribe(
                            () -> {
                                waitLock.get().countDown();
                            },
                            e -> {
                                waitLock.get().countDown();
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
                            SimpleStorage.get(b.bid).subscribe(
                                    pb -> {
                                        pulledBundles.add(pb);
                                        s.onComplete();
                                    },
                                    e -> {
                                        System.out.println("error pulling bundle: " + e.getMessage());
                                        s.onComplete();
                                    })))
                    .subscribe(
                            () -> waitLock.get().countDown(),
                            e -> waitLock.get().countDown());
            waitFinish();
            assertEquals(bundles.length, pulledBundles.size());
            assertFileStorageSize(bundles.length, dir);

            /* check that they are the same */
            for (Bundle bundle : pulledBundles) {
                boolean found = false;
                for (int j = 0; j < bundles.length; j++) {
                    if (bundles[j].bid.getBIDString().equals(bundle.bid.getBIDString())) {
                        found = true;
                        assertArrayEquals(
                                flowableToByteArray(bundles[j].getPayloadBlock().data.observe()),
                                flowableToByteArray(bundle.getPayloadBlock().data.observe()));
                    }
                }
                assertTrue(found);
            }

            /* check remove path */
            System.out.println("[.] remove path from SimpleStorage configuration");
            paths.clear();
            DTNConfiguration.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
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
            paths.add(System.getProperty("path"));
            DTNConfiguration.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
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

    private byte[] flowableToByteArray(Flowable<ByteBuffer> f) {
        AtomicInteger size = new AtomicInteger();
        f.subscribe(b -> size.addAndGet(b.remaining()));
        ByteBuffer ret = ByteBuffer.allocate(size.get());
        f.subscribe(ret::put);
        return ret.array();
    }

    private void clearStorage() {
        cockLock();
        Storage.clear().subscribe(
                () -> waitLock.get().countDown(),
                e -> waitLock.get().countDown()
        );
        waitFinish();
    }

    static void assertStorageSize(int expectedSize) {
        assertEquals(expectedSize, SimpleStorage.count());
    }

    static void assertFileStorageSize(int expectedSize, File dir) {
        assertEquals(expectedSize, dir.listFiles().length);
    }


}
