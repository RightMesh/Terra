package io.left.rightmesh.libdtn.core.storage;

import org.junit.Test;

import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
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

            DTNConfiguration conf = new DTNConfiguration();
            conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
            conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(false);
            Storage storage = new Storage(conf, new Logger(conf));

            Bundle[] bundles = {
                    TestBundle.testBundle1(),
                    TestBundle.testBundle2(),
                    TestBundle.testBundle3(),
                    TestBundle.testBundle4(),
                    TestBundle.testBundle5(),
                    TestBundle.testBundle6()
            };

            System.out.println("[.] clear VolatileStorage");
            storage.getVolatileStorage().clear().subscribe();
            assertEquals(0, storage.getVolatileStorage().count());

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
