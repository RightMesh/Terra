package io.left.rightmesh.libdtn.network;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Test;
import io.left.rightmesh.libdtn.network.cla.STCP;
import io.left.rightmesh.librxtcp.RxTCP;
import io.reactivex.Flowable;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

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

        STCP.open("127.0.0.1", 4591)
                .subscribe(
                        dtnChannel -> {
                            Bundle[] bundles = {
                                    BundleV7Test.testBundle1(),
                                    BundleV7Test.testBundle2(),
                                    BundleV7Test.testBundle3(),
                                    BundleV7Test.testBundle4(),
                                    BundleV7Test.testBundle5(),
                                    BundleV7Test.testBundle6()
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
            BundleV7Test.checkBundlePayload(recv[j]);
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
                                    BundleV7Test::checkBundlePayload,
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
            STCP.open("127.0.0.1", 4592)
                    .subscribe(
                            dtnChannel -> {
                                Bundle[] bundles = {
                                        BundleV7Test.testBundle1(),
                                        BundleV7Test.testBundle2(),
                                        BundleV7Test.testBundle3(),
                                        BundleV7Test.testBundle4(),
                                        BundleV7Test.testBundle5(),
                                        BundleV7Test.testBundle6()
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
