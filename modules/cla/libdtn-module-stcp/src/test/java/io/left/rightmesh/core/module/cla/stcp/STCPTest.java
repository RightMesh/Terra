package io.left.rightmesh.core.module.cla.stcp;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.reactivex.Flowable;

import static junit.framework.TestCase.fail;

/**
 * @author Lucien Loiseau on 26/09/18.
 */
public class STCPTest {

    @Test
    public void testServerOneClient() {
        System.out.println("[+] stcp: testing one server and one client");

        CountDownLatch lock = new CountDownLatch(1);

        Bundle[] recv = {null, null, null, null, null, null};
        int[] i = {0};

        CLA eid = null;
        try {
            eid = CLASTCP.create("127.0.0.1:4591", "/test");
        } catch(EID.EIDFormatException efe) {
            fail();
        }
        new STCP()
                .setPort(4591)
                .start()
                .subscribe(
                        channel -> {
                            channel.recvBundle().subscribe(
                                    b -> {
                                        recv[i[0]++] = b;
                                    },
                                    e -> {
                                        lock.countDown();
                                    },
                                    () -> {
                                        lock.countDown();
                                    });
                        },
                        e -> {
                            fail();
                            lock.countDown();
                        });

        new STCP().open(eid)
                .subscribe(
                        dtnChannel -> {
                            Bundle[] bundles = {
                                    TestBundle.testBundle1(),
                                    TestBundle.testBundle2(),
                                    TestBundle.testBundle3(),
                                    TestBundle.testBundle4(),
                                    TestBundle.testBundle5(),
                                    TestBundle.testBundle6()
                            };
                            dtnChannel
                                    .sendBundles(Flowable.fromArray(bundles))
                                    .subscribe(
                                            j -> {
                                                // ignore
                                            },
                                            e -> {
                                                // ignore
                                            },
                                            dtnChannel::close);
                        },
                        e -> {
                            fail();
                            lock.countDown();
                        });

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        // check payload
        for (int j = 0; j < 6; j++) {
            TestBundle.checkBundlePayload(recv[j]);
        }
    }


    @Test
    public void testServerTenClient() {
        System.out.println("[+] stcp: testing one server and ten clients");

        CountDownLatch lock = new CountDownLatch(10);

        CLA eid = null;
        try {
            eid = CLASTCP.create("127.0.0.1:4592", "/test");
        } catch(EID.EIDFormatException efe) {
            fail();
        }
        new STCP()
                .setPort(4592)
                .start()
                .subscribe(
                        channel -> {
                            channel.recvBundle().subscribe(
                                    TestBundle::checkBundlePayload,
                                    e -> {
                                        lock.countDown();
                                    },
                                    lock::countDown);
                        },
                        e -> {
                            fail();
                            lock.countDown();
                        });

        for (int k = 0; k < 10; k++) {
            new STCP().open(eid)
                    .subscribe(
                            dtnChannel -> {
                                Bundle[] bundles = {
                                        TestBundle.testBundle1(),
                                        TestBundle.testBundle2(),
                                        TestBundle.testBundle3(),
                                        TestBundle.testBundle4(),
                                        TestBundle.testBundle5(),
                                        TestBundle.testBundle6()
                                };
                                dtnChannel
                                        .sendBundles(Flowable.fromArray(bundles))
                                        .subscribe(
                                                j -> {
                                                    // ignore
                                                },
                                                e -> {
                                                    // ignore
                                                },
                                                dtnChannel::close);
                            },
                            e -> {
                                fail();
                                lock.countDown();
                            });
        }

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

}
