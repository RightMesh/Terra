package io.left.rightmesh.libdtn.storage;

import org.junit.Test;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;
import io.left.rightmesh.libdtn.storage.bundle.BundleStorage;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.libdtn.storage.bundle.VolatileStorage;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public class VolatileStorageTest {

    @Test
    public void testVolatileStoreBundle() {
        synchronized (StorageTest.lock) {
            System.out.println("[+] Volatile Storage");
            VolatileStorage.getInstance();
            DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
            DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(false);

            Bundle[] bundles = {
                    BundleV7Test.testBundle1(),
                    BundleV7Test.testBundle2(),
                    BundleV7Test.testBundle3(),
                    BundleV7Test.testBundle4(),
                    BundleV7Test.testBundle5(),
                    BundleV7Test.testBundle6()
            };

            System.out.println("[.] clear VolatileStorage");
            VolatileStorage.clear().subscribe();
            assertEquals(0, VolatileStorage.count());

            System.out.println("[.] store bundle in VolatileStorage");
            for (int i = 0; i < bundles.length; i++) {
                final int j = i;
                VolatileStorage.store(bundles[j]).subscribe(
                        (b) -> {
                            assertEquals(j + 1, VolatileStorage.count());
                            assertEquals(true, Storage.containsVolatile(b.bid));
                        },
                        e -> fail());
            }
            assertEquals(bundles.length, VolatileStorage.count());

            System.out.println("[.] clear VolatileStorage");
            VolatileStorage.clear().subscribe();
            assertEquals(0, VolatileStorage.count());
        }
    }

}
