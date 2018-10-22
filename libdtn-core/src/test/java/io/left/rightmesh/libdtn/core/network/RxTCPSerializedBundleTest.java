package io.left.rightmesh.libdtn.core.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.core.storage.TestBundle;
import io.left.rightmesh.libdtn.core.storage.blob.Factory;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.librxtcp.RxTCP;

import static junit.framework.TestCase.fail;

/**
 * @author Lucien Loiseau on 21/09/18.
 */
public class RxTCPSerializedBundleTest {

    @Test
    public void testServerOneClient() {
        System.out.println("[+] rxtcp: testing bundle serialization / parsing over RxTCP");

        CountDownLatch lock = new CountDownLatch(6);
        Bundle[] recv = {null, null, null, null, null, null};
        int[] i = {0};


        new RxTCP.SimpleServer(4561)
                .start().subscribe(
                connection -> {
                    // prepare parser
                    CborParser p = BundleV7Parser.create(b -> {
                        recv[i[0]++] = b;
                    }, Factory.getInstance());

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
