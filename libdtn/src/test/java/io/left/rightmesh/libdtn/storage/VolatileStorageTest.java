package io.left.rightmesh.libdtn.storage;

import org.junit.Test;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;
import io.left.rightmesh.libdtn.storage.bundle.BundleStorage;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.libdtn.storage.bundle.VolatileStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public class VolatileStorageTest {

    @Test
    public void testVolatileStoreBundle() {
        System.out.println("[+] storage: test store one bundle in volatile storage");
        VolatileStorage.init();

        Bundle[] bundles = {
                BundleV7Test.testBundle1(),
                BundleV7Test.testBundle2(),
                BundleV7Test.testBundle3(),
                BundleV7Test.testBundle4(),
                BundleV7Test.testBundle5(),
                BundleV7Test.testBundle6()
        };

        VolatileStorage storage = VolatileStorage.getInstance();
        VolatileStorage.removeVolatileBundle().subscribe();

        assertEquals(0, VolatileStorage.count());

        for (int i = 0; i < bundles.length; i++) {
            final int  j = i;
            storage.store(bundles[j]).subscribe(
                    (b) -> {
                        assertEquals(j+1, VolatileStorage.count());
                        assertEquals(true, Storage.containsVolatile(b.bid));
                    },
                    e -> fail());
        }

        assertEquals(bundles.length, VolatileStorage.count());

        VolatileStorage.removeVolatileBundle().subscribe();
        assertEquals(0, VolatileStorage.count());
    }

}
