package io.left.rightmesh.libdtn.network;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Lucien Loiseau on 17/09/18.
 */
public class RxTCPTest {

    @Test
    public void testServerOneClient() {
        System.out.println("[+] rxtcp: testing 1 Server and 1 Client");

        CountDownLatch lock = new CountDownLatch(1);

        String test = "testing rxtcp client and server";
        byte[][] recv = {null};

        new RxTCP.Server(4556).start().subscribe(
                connection -> {
                    //System.out.println("< client connected");
                    connection.recv().subscribe(
                            buffer -> {
                                //System.out.println("< recv " + buffer.remaining() + " bytes");
                                byte[] buf = new byte[buffer.remaining()];
                                buffer.get(buf, 0, buffer.remaining());
                                //System.out.println("  buffer: " + new String(buf));
                                recv[0] = buf;
                                lock.countDown();
                            },
                            e -> {
                                fail();
                                lock.countDown();
                            });
                },
                e -> {
                    fail();
                    lock.countDown();
                });

        new RxTCP.ConnectionRequest("127.0.0.1", 4556).connect().subscribe(
                connection -> {
                    //System.out.println("> connected to server");
                    connection.send(test.getBytes());
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

        assertNotNull(recv[0]);
        assertArrayEquals(test.getBytes(), recv[0]);
    }

    @Test
    public void testServerTenClients() {
        System.out.println("[+] rxtcp: testing 1 Server and 10 Clients");

        CountDownLatch lock = new CountDownLatch(10);

        String test = "testing rxtcp client and server";
        byte[][] recv = {null, null, null, null, null, null, null, null, null, null};

        new RxTCP.Server(4557).start().subscribe(
                connection -> {
                    connection.recv().subscribe(
                            buffer -> {
                                int k = buffer.get();
                                byte[] buf = new byte[buffer.remaining()];
                                buffer.get(buf, 0, buffer.remaining());
                                recv[k] = buf;
                                lock.countDown();
                            },
                            e -> {
                                fail();
                                lock.countDown();
                            });
                },
                e -> {
                    fail();
                    lock.countDown();
                });

        for (int i = 0; i < 10; i++) {
            {
                final int j = i;
                new RxTCP.ConnectionRequest("127.0.0.1", 4557).connect().subscribe(
                        connection -> {
                            //System.out.println("> " + j + " connected to server");
                            byte[] header = {(byte) (j & 0xff)};
                            connection.send(ArrayUtils.addAll(header, (test + ":" + j).getBytes()));
                        },
                        e -> {
                            //System.out.println("connection failed");
                            fail();
                            lock.countDown();
                        });
            }
        }

        try {
            lock.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }

        for (int i = 0; i < 10; i++) {
            assertNotNull(recv[i]);
            assertArrayEquals((test + ":" + i).getBytes(), recv[i]);
        }
    }

    @Test
    public void testServerStressTest() {
        System.out.println("[+] rxtcp: stress test Server and 1 Client (1000 * 4096 bytes)");

        CountDownLatch lock = new CountDownLatch(1);

        int BUFSIZE = 4096;
        final ByteBuffer sendBuffer = ByteBuffer.allocate(BUFSIZE);
        int totalSend = 1000;
        int[] recv = {0};

        new RxTCP.Server(4558).start().subscribe(
                connection -> {
                    connection.recv().subscribe(
                            buffer -> {
                                recv[0] += buffer.remaining();
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

        new RxTCP.ConnectionRequest("127.0.0.1", 4558).connect().subscribe(
                connection -> {
                    sendBuffer.clear();
                    connection.send(Flowable.generate(
                            () -> 0L,
                            (state, s) -> {
                                if (state == totalSend) {
                                    s.onComplete();
                                } else {
                                    sendBuffer.clear();
                                    s.onNext(sendBuffer);
                                }
                                return state + 1;
                            })
                    );
                    connection.closeJobsDone();
                },
                e -> {
                    //System.out.println("connection failed");
                    fail();
                    lock.countDown();
                });

        long before = System.nanoTime();
        try {
            lock.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
        long timeSpent = (System.nanoTime() - before)+1;
        //System.out.println(">> StressTest :: send="+totalSend*BUFSIZE+" bytes, spent="+timeSpent+" nanoseconds, recv="+recv[0]+" bytes");
        assertEquals(totalSend*BUFSIZE, recv[0]);
    }


    @Test
    public void testFailedConnection() {
        System.out.println("[+] rxtcp: test failed connection");

        CountDownLatch lock = new CountDownLatch(1);

        new RxTCP.ConnectionRequest("127.1.5.9", 8765).retry(3,2000).connect().subscribe(
                connection -> {
                    lock.countDown();
                },
                e -> {
                    //System.out.println("could not connect to 127.1.5.9:8765");
                    lock.countDown();
                });

        long before = System.currentTimeMillis();
        try {
            lock.await(7, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // ignore
        }
    }
}
