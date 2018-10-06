package io.left.rightmesh.libdtn.storage;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.SIMPLE_STORAGE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Lucien Loiseau on 04/10/18.
 */
public class SimpleStorageTest {

    @Test
    public void testSimpleStoreBundle() {
        System.out.println("[+] storage: test store bundles in simple storage");

        Set<String> paths = new HashSet<>();
        paths.add(System.getProperty("path"));
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

        SimpleStorage.clear();
        SimpleStorage.count().subscribe(
                i -> assertEquals(0, i.intValue()),
                e -> {
                    System.out.println("cannot count: "+e.getMessage());
                    fail();
                });

        for (int i = 0; i < bundles.length; i++) {
            final int j = i;
            SimpleStorage.store(bundles[j]).subscribe(
                    () -> {
                        SimpleStorage.count().subscribe(
                                k -> assertEquals(j + 1, k.intValue()),
                                e -> {
                                    System.out.println("cannot count: "+e.getMessage());
                                    fail();
                                });
                        SimpleStorage.contains(bundles[j].bid).subscribe(
                                b -> assertEquals(true, b),
                                e -> {
                                    System.out.println("cannot contain: "+e.getMessage());
                                    fail();
                                });
                    },
                    e -> fail());
        }

        SimpleStorage.count().subscribe(
                i -> assertEquals(bundles.length, i.intValue()),
                e -> {
                    System.out.println("cannot count: "+e.getMessage());
                    fail();
                });

        SimpleStorage.clear().subscribe(
                () -> SimpleStorage.count().subscribe(
                        i -> assertEquals(0, i.intValue()),
                        e -> {
                            System.out.println("cannot count: "+e.getMessage());
                            fail();
                        }),
                e -> {
                    System.out.println("cannot clear: "+e.getMessage());
                    fail();
                });
    }
}
