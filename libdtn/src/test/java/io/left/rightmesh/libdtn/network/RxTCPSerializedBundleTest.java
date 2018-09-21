package io.left.rightmesh.libdtn.network;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PayloadBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 21/09/18.
 */
public class RxTCPSerializedBundleTest {


    String testPayload = "This is a test for bundle serialization";


    Bundle testBundle0() {
        Bundle bundle = new Bundle();
        bundle.destination = EID.createIPN(5, 12);
        bundle.source = EID.createDTN("source");
        bundle.reportto = EID.NullEID();
        return bundle;
    }

    Bundle testBundle1() {
        Bundle bundle = testBundle0();
        bundle.addBlock(new PayloadBlock(testPayload));
        return bundle;
    }

    Bundle testBundle2() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        return bundle;
    }

    Bundle testBundle3() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        return bundle;
    }

    Bundle testBundle4() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        return bundle;
    }


    Bundle testBundle5() {
        Bundle bundle = testBundle1();
        bundle.addBlock(new AgeBlock());
        bundle.addBlock(new ScopeControlHopLimitBlock());
        bundle.addBlock(new PreviousNodeBlock(EID.generate()));
        bundle.crcType = PrimaryBlock.CRCFieldType.CRC_32;
        return bundle;
    }

    Bundle testBundle6() {
        Bundle bundle = testBundle0();
        bundle.crcType = PrimaryBlock.CRCFieldType.CRC_32;

        Block age = new AgeBlock();
        Block scope = new ScopeControlHopLimitBlock();
        Block payload = new PayloadBlock(testPayload);
        Block previous = new PreviousNodeBlock();

        age.crcType = BlockHeader.CRCFieldType.CRC_16;
        scope.crcType = BlockHeader.CRCFieldType.CRC_16;
        payload.crcType = BlockHeader.CRCFieldType.CRC_32;
        previous.crcType = BlockHeader.CRCFieldType.CRC_32;

        bundle.addBlock(age);
        bundle.addBlock(scope);
        bundle.addBlock(payload);
        bundle.addBlock(previous);
        return bundle;
    }

    @Test
    public void testServerOneClient() {
        System.out.println("[+] testing bundle serialization / parsing over RxTCP");

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
                            testBundle1(),
                            testBundle2(),
                            testBundle3(),
                            testBundle4(),
                            testBundle5(),
                            testBundle6()
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
                assertEquals(testPayload, payload[0]);
            }
        }
    }

}
