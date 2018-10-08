package io.left.rightmesh.libdtn.storage;

import org.junit.Test;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public class VolatileStorageTest {

    @Test
    public void testVolatileStoreBundle() {
        System.out.println("[+] storage: test store one bundle in volatile storage");

        Bundle[] bundles = {
                BundleV7Test.testBundle1(),
                BundleV7Test.testBundle2(),
                BundleV7Test.testBundle3(),
                BundleV7Test.testBundle4(),
                BundleV7Test.testBundle5(),
                BundleV7Test.testBundle6()
        };

        VolatileStorage storage = VolatileStorage.getInstance();
        storage.clear();
        try {
            assertEquals(0, storage.count());
        } catch(BundleStorage.StorageUnavailableException e) {
            fail();
        }

        for (int i = 0; i < bundles.length; i++) {
            final int  j = i;
            storage.store(bundles[j]).subscribe(
                    () -> {
                        try {
                            assertEquals(j+1, storage.count());
                        } catch(BundleStorage.StorageUnavailableException e) {
                            fail();
                        }

                        try {
                            assertEquals(true, storage.contains(bundles[j].bid));
                        } catch(BundleStorage.StorageUnavailableException e) {
                            fail();
                        }
                    },
                    e -> fail());
        }

        try {
            assertEquals(bundles.length, storage.count());
        } catch(BundleStorage.StorageUnavailableException e) {
            fail();
        }

        storage.clear().subscribe();
        try {
            assertEquals(0, storage.count());
        } catch(BundleStorage.StorageUnavailableException e) {
            fail();
        }
    }

}
