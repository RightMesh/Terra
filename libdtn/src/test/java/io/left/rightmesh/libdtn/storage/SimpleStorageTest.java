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
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;
import io.left.rightmesh.libdtn.storage.bundle.BundleStorage;
import io.left.rightmesh.libdtn.storage.bundle.SimpleStorage;
import io.left.rightmesh.libdtn.utils.Log;
import io.reactivex.Flowable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.LOG_LEVEL;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 04/10/18.
 */
public class SimpleStorageTest {

    @Test
    public void testSimpleStoreBundle() {
        System.out.println("[+] storage: test store bundles in simple storage");
        DTNConfiguration.<Log.LOGLevel>get(LOG_LEVEL).update(Log.LOGLevel.INFO);
        Set<String> paths = new HashSet<>();
        paths.add(System.getProperty("path"));
        File dir = new File(System.getProperty("path")+"/bundle/");
        DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(true);
        DTNConfiguration.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
        SimpleStorage.init();

        Bundle[] bundles = {
                BundleV7Test.testBundle1(),
                BundleV7Test.testBundle2(),
                BundleV7Test.testBundle3(),
                BundleV7Test.testBundle4(),
                BundleV7Test.testBundle5(),
                BundleV7Test.testBundle6()
        };

        final AtomicReference<CountDownLatch> lock = new AtomicReference<>(new CountDownLatch(1));

        System.out.println("[+] clear storage");
        clearStorage();
        assertStorageSize(0);
        assertFileStorageSize(0, dir);


        /* store the bundles in storage */
        System.out.println("[+] store in storage");
        lock.set(new CountDownLatch(6));
        for (int i = 0; i < bundles.length; i++) {
            final int j = i;
            SimpleStorage.store(bundles[j]).subscribe(
                    (b) -> lock.get().countDown(),
                    e -> {
                        System.out.println("error storing bundle: "+e.getMessage());
                        lock.get().countDown();
                    });
        }
        try {
            lock.get().await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
        assertStorageSize(6);
        assertFileStorageSize(6, dir);

        /* pull the bundles from storage  */
        System.out.println("[+] pull from storage");
        lock.set(new CountDownLatch(6));
        final LinkedList<Bundle> pulledBundles = new LinkedList<>();
        for (Bundle bundle : bundles) {
            SimpleStorage.get(bundle.bid).subscribe(
                    b -> {
                        pulledBundles.add(b);
                        lock.get().countDown();
                    },
                    e -> {
                        System.out.println("error pulling bundle from storage: "+e.getMessage());
                        e.printStackTrace();
                        lock.get().countDown();
                    });
        }

        try {
            lock.get().await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
        assertEquals(6, pulledBundles.size());
        assertFileStorageSize(6, dir);

        /* check that they are the same */
        for (Bundle bundle : pulledBundles) {
            boolean found = false;
            for(int j = 0; j < bundles.length; j++) {
                if(bundles[j].bid.toString().equals(bundle.bid.toString())) {
                    found = true;
                    assertArrayEquals(
                            flowableToByteArray(bundles[j].getPayloadBlock().data.observe()),
                            flowableToByteArray(bundle.getPayloadBlock().data.observe()));
                }
            }
            assertTrue(found);
        }

        /* check remove path */
        System.out.println("[+] remove path from configuration");
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
        System.out.println("[+] add path to configuration for indexing");
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
        System.out.println("[+] clear again");
        clearStorage();
        assertStorageSize(0);
        assertFileStorageSize(0, dir);
    }

    private byte[] flowableToByteArray(Flowable<ByteBuffer> f) {
        AtomicInteger size = new AtomicInteger();
        f.subscribe(b -> size.addAndGet(b.remaining()));
        ByteBuffer ret = ByteBuffer.allocate(size.get());
        f.subscribe(ret::put);
        return ret.array();
    }

    private void clearStorage() {
        final AtomicReference<CountDownLatch> lock = new AtomicReference<>(new CountDownLatch(1));
        lock.set(new CountDownLatch(1));
        SimpleStorage.clear().subscribe(
                () -> lock.get().countDown(),
                e -> {
                    System.out.println("[!] cannot clear storage: "+e.getMessage());
                    e.printStackTrace();
                    lock.get().countDown();
                });
        try {
            lock.get().await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    private void assertStorageSize(int expectedSize) {
        assertEquals(expectedSize, SimpleStorage.count());
    }

    private void assertFileStorageSize(int expectedSize, File dir) {
        assertEquals(expectedSize, dir.listFiles().length);
    }


}
