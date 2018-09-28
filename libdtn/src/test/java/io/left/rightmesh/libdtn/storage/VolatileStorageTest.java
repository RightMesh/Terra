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
    public void testStoreBundle() {
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
        storage.count().subscribe(i -> assertEquals(0, i.intValue()));

        for (int i = 0; i < bundles.length; i++) {
            final int  j = i;
            storage.store(bundles[j]).subscribe(
                    () -> {
                        storage.count().subscribe(k -> assertEquals(j+1, k.intValue()));
                        storage.contains(bundles[j].bid).subscribe(b -> assertEquals(true, b));
                    },
                    e -> {
                        System.out.println(e.getMessage());
                        fail();
                    });
        }
        
        storage.count().subscribe(i -> assertEquals(bundles.length, i.intValue()));
        storage.clear().subscribe(
                () -> storage.count().subscribe(i -> assertEquals(0, i.intValue())));
    }

}
