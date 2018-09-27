package io.left.rightmesh.libdtn.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 21/09/18.
 */
public class RxTCPSerializedBundleTest {

    @Test
    public void testServerOneClient() {
        System.out.println("[+] rxtcp: testing bundle serialization / parsing over RxTCP");

        CountDownLatch lock = new CountDownLatch(1);

        Bundle[] recv = {null, null, null, null, null, null};
        int[] i = {0};

        new RxTCP.Server(4561).start().subscribe(
                connection -> {
                    // prepare parser
                    CborParser p = BundleV7Parser.create(b -> {
                        recv[i[0]++] = b;
                    });

                    connection.recv().subscribe(
                            buffer -> {
                                try {
                                    if (p.read(buffer)) {
                                        p.reset();
                                    }
                                } catch (RxParserException rpe) {
                                    rpe.printStackTrace();
                                    Assert.fail();
                                }
                            },
                            e -> lock.countDown());
                },
                e -> {
                    fail();
                    lock.countDown();
                });

        new RxTCP.ConnectionRequest("127.0.0.1", 4561).connect().subscribe(
                connection -> {
                    //System.out.println("> connected to server");
                    Bundle[] bundles = {
                            BundleV7Test.testBundle1(),
                            BundleV7Test.testBundle2(),
                            BundleV7Test.testBundle3(),
                            BundleV7Test.testBundle4(),
                            BundleV7Test.testBundle5(),
                            BundleV7Test.testBundle6()
                    };

                    for(Bundle bundle : bundles) {
                        connection.send(BundleV7Serializer.encode(bundle).observe(20));
                    }

                    connection.closeJobsDone();
                },
                e -> {
                    //System.out.println("connection failed");
                    fail();
                    lock.countDown();
                });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        // check payload
        for(int j = 0; j < 6; j++) {
            checkBundlePayload(recv[j]);
        }
    }


    void checkBundlePayload(Bundle bundle) {
        // assert
        assertEquals(true, bundle != null);
        String[] payload = {null};
        if (bundle != null) {
            for(Block block : bundle.getBlocks()) {
                assertEquals(true, block.crc_ok);
            }

            bundle.getPayloadBlock().data.observe().subscribe(
                    buffer -> {
                        byte[] arr = new byte[buffer.remaining()];
                        buffer.get(arr);
                        payload[0] = new String(arr);
                    });

            assertEquals(true, payload[0] != null);
            if (payload[0] != null) {
                assertEquals(BundleV7Test.testPayload, payload[0]);
            }
        }
    }

}
