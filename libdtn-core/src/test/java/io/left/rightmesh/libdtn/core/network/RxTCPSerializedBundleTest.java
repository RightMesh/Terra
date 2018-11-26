package io.left.rightmesh.libdtn.core.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BaseBlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.libdtn.common.utils.SimpleLogger;
import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.core.MockBlockManager;
import io.left.rightmesh.libdtn.core.MockCore;
import io.left.rightmesh.libdtn.core.api.BlockManagerAPI;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.storage.TestBundle;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.core.storage.Storage;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.left.rightmesh.librxtcp.RxTCP;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.SIMPLE_STORAGE_PATH;
import static junit.framework.TestCase.fail;

/**
 * @author Lucien Loiseau on 21/09/18.
 */
public class RxTCPSerializedBundleTest {


    /* mocking the core */
    public CoreAPI mockCore() {
        return new MockCore() {
            @Override
            public ConfigurationAPI getConf() {
                DTNConfiguration conf = new DTNConfiguration();
                conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(true);
                conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(false);
                return conf;
            }

            @Override
            public BlockManagerAPI getBlockManager() {
                return new MockBlockManager() {
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
    public void testServerOneClient() {
        System.out.println("[+] rxtcp: testing bundle serialization / parsing over RxTCP");
        Storage storage = new Storage(mockCore());

        CountDownLatch lock = new CountDownLatch(6);
        Bundle[] recv = {null, null, null, null, null, null};
        int[] i = {0};

        new RxTCP.SimpleServer(4561)
                .start().subscribe(
                connection -> {
                    // prepare parser
                    BundleV7Item bundleParser = new BundleV7Item(new NullLogger(), storage.getBlobFactory());
                    CborParser p = CBOR.parser().cbor_parse_custom_item(
                            () -> new BundleV7Item(new NullLogger(), storage.getBlobFactory()),
                            (__, ___, item) ->recv[i[0]++] = item.bundle
                    );

                    connection.recv().subscribe(
                            buffer -> {
                                try {
                                    while(buffer.hasRemaining()) {
                                        if (p.read(buffer)) {
                                            p.reset();
                                            lock.countDown();
                                        }
                                    }
                                } catch (RxParserException rpe) {
                                    rpe.printStackTrace();
                                    Assert.fail();
                                }
                            },
                            e -> {});
                },
                e -> fail());

        new RxTCP.SimpleConnectionRequest("127.0.0.1", 4561).connect().subscribe(
                connection -> {
                    Bundle[] bundles = {
                            TestBundle.testBundle1(),
                            TestBundle.testBundle2(),
                            TestBundle.testBundle3(),
                            TestBundle.testBundle4(),
                            TestBundle.testBundle5(),
                            TestBundle.testBundle6()
                    };

                    for(Bundle bundle : bundles) {
                        connection.send(BundleV7Serializer.encode(bundle).observe(2048));
                    }

                    connection.closeJobsDone();
                },
                e -> fail());

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        // check payload
        for(int j = 0; j < 6; j++) {
            TestBundle.checkBundlePayload(recv[j]);
        }
    }

}
