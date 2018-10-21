package io.left.rightmesh.libdtn.network;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libdtn.storage.TestBundle;
import io.left.rightmesh.libdtncommon.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.STCP;
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

        new STCP().open("127.0.0.1", 4591)
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
            new STCP().open("127.0.0.1", 4592)
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
