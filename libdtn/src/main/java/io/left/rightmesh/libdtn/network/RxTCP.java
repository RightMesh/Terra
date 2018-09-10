package io.left.rightmesh.libdtn.network;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subscribers.DisposableSubscriber;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Single-Threaded - Reactive TCP Class.
 *
 * @author Lucien Loiseau on 04/08/18.
 */
public class RxTCP {

    public static final Object lock = new Object();
    public static NIOEngine instance = null;

    private static NIOEngine nio() throws IOException {
        synchronized (lock) {
            if (instance == null) {
                instance = new NIOEngine();
            }
            return instance;
        }
    }

    private interface NIOConnectCallback {
        void onConnectEvent(SelectionKey key);
    }

    private interface NIOAcceptCallback {
        void onAcceptEvent(SelectionKey key);
    }

    private interface NIOReadCallback {
        void onReadEvent(SelectionKey key);
    }

    private interface NIOWriteCallback {
        void onWriteEvent(SelectionKey key);
    }

    private static class NIOCallback {
        NIOConnectCallback c = null;
        NIOAcceptCallback a = null;
        NIOReadCallback r = null;
        NIOWriteCallback w = null;
    }

    /**
     * NIOEngine is an event loop for the Java NIO. It listens for the Selector and pushes event
     * whenever there are any.
     */
    private static class NIOEngine {

        private class RegisterJob {
            SingleEmitter s;
            SelectableChannel channel;
            int op;

            RegisterJob(SingleEmitter s, SelectableChannel channel, int op) {
                this.s = s;
                this.channel = channel;
                this.op = op;
            }
        }

        private Selector selector;
        private Queue<RegisterJob> registerJobQueue;
        private Thread niothread;

        NIOEngine() throws IOException {
            registerJobQueue = new ConcurrentLinkedQueue<>();
            selector = Selector.open();
            createNIOEventLoop();
        }

        /**
         * returns the Selector used for this engine.
         *
         * @return Selector
         */
        public Selector selector() {
            return selector;
        }

        /**
         * observe the events thrown by the NIO engine.
         *
         * @return Observable to observe the NIO events
         * public Observable<SelectionKey> events() {
         * return subject;
         * }
         */

        private void createNIOEventLoop() {
            new Thread(() -> {
                try {
                    niothread = Thread.currentThread();
                    niothread.setName("NIO Thread - DO NOT BLOCK");
                    while (true) {
                        selector.select();
                        Iterator keys = selector.selectedKeys().iterator();
                        while (keys.hasNext()) {
                            SelectionKey key = (SelectionKey) keys.next();
                            keys.remove();
                            if (!key.isValid()) {
                                continue;
                            }

                            Object o = key.attachment();
                            if ((o != null) && (o instanceof NIOCallback)) {
                                NIOCallback cb = (NIOCallback) o;
                                if (key.isReadable() && (cb.r != null)) {
                                    cb.r.onReadEvent(key);
                                }
                                if (key.isWritable() && (cb.w != null)) {
                                    cb.w.onWriteEvent(key);
                                }
                                if (key.isAcceptable() && (cb.a != null)) {
                                    cb.a.onAcceptEvent(key);
                                }
                                if (key.isConnectable() && (cb.c != null)) {
                                    cb.c.onConnectEvent(key);
                                }
                            }
                        }

                        // check if there is any registration waiting
                        while (registerJobQueue.size() > 0) {
                            RegisterJob job = registerJobQueue.poll();
                            try {
                                SelectionKey key = job.channel.register(selector, job.op);
                                if (key.attachment() == null) {
                                    key.attach(new NIOCallback());
                                }
                                job.s.onSuccess(key);
                            } catch (IOException io) {
                                job.s.onError(io);
                            }
                        }
                    }
                } catch (IOException io) {
                    // do nothing
                } finally {
                    /*
                     * tell callback that an error happened
                    for(SelectionKey key : selector.keys()) {
                        NIOCallback cb = ((NIOCallback)key.attachment());
                        if(cb.a != null) {
                            cb.a.onAcceptEvent(null);
                        }
                        if(cb.c != null) {
                            cb.c.onConnectEvent(null);
                        }
                        if(cb.r != null) {
                            cb.r.onReadEvent(null);
                        }
                        if(cb.w != null) {
                            cb.w.onWriteEvent(null);
                        }
                    }
                    */
                    try {
                        selector.close();
                    } catch (IOException io) {
                        // ignore
                    }
                    selector = null;
                }
            }).start();
        }

        /**
         * queue a registration so that it will be done by the NIO thread.
         *
         * @param channel to register
         * @param op      event to listen to
         * @return the registered SelectionKey
         */
        public Single<SelectionKey> register(SelectableChannel channel, int op) {
            if (niothread != null && Thread.currentThread().equals(niothread)) {
                try {
                    return Single.just(channel.register(selector, op));
                } catch (ClosedChannelException cce) {
                    return Single.error(cce);
                }
            } else {
                return Single.create(s -> {
                    registerJobQueue.add(new RegisterJob(s, channel, op));
                    selector.wakeup();
                });
            }
        }
    }

    /**
     * A Reactive TCP Server.
     */
    public static class Server {

        private int port;
        private ConnectionFactory factory;
        private ServerSocketChannel channel;
        private SelectionKey key;

        /**
         * Default Constructor.
         *
         * @param port port to listen to
         */
        public Server(int port) {
            this.port = port;
            this.factory = defaultFactory;
        }

        /**
         * Constructor.
         *
         * @param port    port to listen to
         * @param factory Connection factory
         */
        public Server(int port, ConnectionFactory factory) {
            this(port);
            this.factory = factory;
        }

        /**
         * create a new Observable that whenever subscribed to, starts the server and
         * emits a new Connection every time a client connects to the server.
         * The new connection event is emitted in a new thread so as not to block the NIO thread.
         *
         * @return an Observable to keep track of every new Connection
         */
        public Observable<Connection> start() {
            if (channel != null) {
                return Observable.error(new Throwable("Server already started"));
            }
            return Observable.<Connection>create(s -> {
                try {
                    channel = ServerSocketChannel.open();
                    InetSocketAddress listenAddress = new InetSocketAddress(port);
                    channel.socket().bind(listenAddress);
                    channel.configureBlocking(false);
                } catch (IOException io) {
                    s.onError(io);
                }
                nio().register(channel, SelectionKey.OP_ACCEPT).subscribe(
                        registeredKey -> {
                            key = registeredKey;
                            // accept callback
                            ((NIOCallback) key.attachment()).a = (key) -> {
                                try {
                                    s.onNext(factory.create(channel.accept()));
                                } catch (IOException io) {
                                    // silently ignore it
                                }
                            };
                        },
                        s::onError);
            }).observeOn(Schedulers.io());
        }

        /**
         * stop the server from listening for connection.
         */
        public void stop() {
            if (channel == null) {
                return;
            }

            try {
                key.cancel();
                channel.close();
            } catch (IOException io) {
                // ignore
            } finally {
                channel = null;
            }
        }
    }

    /**
     * ConnectionRequest is used to create a connection proactively.
     */
    public static class ConnectionRequest {

        private String host;
        private int port;
        private ConnectionFactory factory;
        private SocketChannel channel;

        /**
         * Create a connection request for a given host and port. By default it will create
         * a {@see Connection} object upon connection.
         *
         * @param host to connect to
         * @param port to connect to
         */
        public ConnectionRequest(String host, int port) {
            this.host = host;
            this.port = port;
            this.factory = defaultFactory;
        }

        /**
         * Create a connection request for a given host and port with a special ConnectionFactory
         * to create the Connection object upon connection.
         *
         * @param host    to connect to
         * @param port    to connect to
         * @param factory to create the Connection object
         */
        public ConnectionRequest(String host, int port, ConnectionFactory factory) {
            this.host = host;
            this.port = port;
            this.factory = factory;
        }

        /**
         * connect() will perform the connection logic and return a new Connection upon success
         * or an error if it fails. The onSuccess event is emitted in a new Thread so as not to
         * block the NIO Thread.
         *
         * @return a new Connection upon success, an error otherwise
         */
        // CHECKSTYLE IGNORE LineLength
        public Single<Connection> connect() {
            if (channel != null) {
                return Single.error(new Throwable("connection already connected"));
            }

            return Single.<Connection>create(s -> {
                try {
                    NIOEngine nio = nio();
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    nio.register(channel, SelectionKey.OP_CONNECT).subscribe(
                            registeredKey -> {
                                // connect callback
                                ((NIOCallback) registeredKey.attachment()).c = (key) -> {
                                    key.interestOps(0);
                                    ((NIOCallback) key.attachment()).c = null;

                                    try {
                                        if (channel.finishConnect()) {
                                            s.onSuccess(factory.create(this.channel));
                                        } else {
                                            s.onError(new Throwable("could not connect"));
                                        }
                                    } catch (IOException io) {
                                        s.onError(new Throwable("could not connect"));
                                    }
                                };
                                channel.connect(new InetSocketAddress(host, port));
                            }
                    );
                } catch (IOException io) {
                    s.onError(new Throwable("could not connect"));
                }
            }).

                    observeOn(Schedulers.io());
        }
        // CHECKSTYLE END IGNORE LineLength
    }

    /**
     * Connection factory, this is used by the server to create a new Connection whenever a client
     * is connected.
     */
    public interface ConnectionFactory {
        Connection create(SocketChannel channel) throws IOException;
    }

    /**
     * The default factory instantiate a Connection.
     */
    private static ConnectionFactory defaultFactory = Connection::new;

    /**
     * Class Connection to send and receive ByteBuffer to a peer over TCP in a reactive way.
     */
    public static class Connection {

        // lock
        private final ReentrantLock lock = new ReentrantLock();

        public SocketChannel channel;
        private NIOEngine nio;
        private SelectionKey key;

        // job queue
        private LinkedBlockingQueue<JobOrder> jobOrderQueue;
        private JobOrder currentOrder;

        /**
         * Constructor for a Reactive Connection.
         *
         * @param channel socket of a connected peer
         * @throws IOException if the socket cannot be tuned in non-blocking mode
         */
        public Connection(SocketChannel channel) throws IOException {
            this.nio = nio();
            this.channel = channel;
            channel.configureBlocking(false);
            jobOrderQueue = new LinkedBlockingQueue<>(5);
            prepareWritePipeline();
        }

        /**
         * Close the current connection.
         */
        public void close() {
            try {
                lock.lock();
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException io) {
                // ignore
            } finally {
                lock.unlock();
            }
        }

        private void cleanup() {
            try {
                lock.lock();

                // already closed
                if (channel == null) {
                    return;
                }

                // unregister from nio event
                if (key != null) {
                    key.cancel();
                    key = null;
                }

                // clear the job queue and notify the clients
                for (JobOrder jobOrder : jobOrderQueue) {
                    jobOrder.sendBufferTask.onError(new Throwable("channel has closed"));
                }
                jobOrderQueue.clear();

                if (((NIOCallback)key.attachment()).w != null) {
                    ((NIOCallback)key.attachment()).w = null;
                }
                if (((NIOCallback)key.attachment()).r != null) {
                    ((NIOCallback)key.attachment()).r = null;
                }

                // effectively close the channel
                try {
                    channel.close();
                } catch (IOException io) {
                    // ignore
                }
            } finally {
                channel = null;
                lock.unlock();
            }
        }

        /**
         * return an Observable for the stream of ByteBuffer read from the socket. It will not be
         * reading the socket until an Observer subscribed to the stream. Note that there can be
         * only one Observer at any given time!
         * <p>
         * <p>The subscriber should try to return as fast as possible as the onNext() event is
         * emitted in the NIO thread. No read nor write operation can be performed until the method
         * returns
         *
         * @return Observable ByteBuffer stream read from the socket
         */
        public Observable<ByteBuffer> recv() {
            if (((NIOCallback)key.attachment()).r != null) {
                return Observable.error(new Throwable("an observer is already subscribed"));
            }
            return Observable.create(s -> {
                ByteBuffer buffer = ByteBuffer.allocate(2048);
                NIOReadCallback rc =  (key) -> {
                    if (s.isDisposed()) { // the Observer has gone
                        turnOffNIOEvent(SelectionKey.OP_READ);
                        ((NIOCallback) key.attachment()).r = null;
                        return;
                    }

                    try {
                        buffer.clear();
                        int numRead = channel.read(buffer);
                        if (numRead == -1) {
                            throw new IOException("Channel closed");
                        }
                        buffer.flip();
                        s.onNext(buffer.slice());

                    } catch (IOException io) { // peer disconnected
                        cleanup();
                        s.onError(io);
                    }
                };
                turnOnNIOEvent(SelectionKey.OP_READ, rc);
            });
        }

        /**
         * Order a job to be transmitted. It will not actually be queued until an Observer
         * subscribed to the jobhandle observe() method.
         *
         * @param job Flowable of ByteBuffer to sendBundle over the socket
         * @return an Observable to keep track of bytes sent
         */
        public JobHandle order(Flowable<ByteBuffer> job) {
            return new JobHandle(job);
        }

        public class JobHandle {
            Flowable<ByteBuffer> job;
            JobOrder order;
            boolean cancelled = false;

            public JobHandle(Flowable<ByteBuffer> job) {
                this.job = job;
                this.cancelled = false;
                this.order = null;
            }

            public Observable<Integer> observe() {
                if (cancelled) {
                    return Observable.error(new Throwable("cancelled order"));
                }

                try {
                    lock.lock();
                    if (channel == null) {
                        return Observable.error(new Throwable("channel has closed"));
                    } else {
                        return Observable.create(s -> {
                            this.order = new JobOrder(s, job);
                            jobOrderQueue.put(this.order);
                            turnOnNIOEvent(SelectionKey.OP_WRITE, null); // will wake up to check the queue
                        });
                    }
                } finally {
                    lock.unlock();
                }
            }

            public boolean cancel() {
                try {
                    lock.lock();
                    cancelled = true;

                    if (order == null) {
                        return true;
                    }

                    order.cancel();
                    if (currentOrder == order) {
                        return true;
                    } else {
                        if (jobOrderQueue.contains(order)) {
                            jobOrderQueue.remove(order);
                            return true;
                        } else {
                            return false;
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }


        private void checkQueue() {
            if (jobOrderQueue.peek() != null) {
                try {
                    currentOrder = jobOrderQueue.take();
                } catch (InterruptedException ie) {
                    // should never happen but if it does will wait for next order
                    return;
                }
                // queue is not empty, we perform the job
                currentOrder.doJob();
            } // if queue is empty go back to sleep
        }

        /**
         * A Job Order consists of a Flowable of ByteBuffer, that is the job and an
         * ObservableEmitter to give the client some feedback about the processing of this order.
         */
        private class JobOrder {
            private Flowable<ByteBuffer> job; // upstream
            private ObservableEmitter<Integer> trackOrder; // downstream
            private int bytesSent;
            private boolean cancelled;

            // buffers
            private ByteBuffer sendBuffer;
            private CompletableEmitter sendBufferTask;


            JobOrder(ObservableEmitter<Integer> trackOrder, Flowable<ByteBuffer> job) {
                this.trackOrder = trackOrder;
                this.job = job;
                this.cancelled = false;
            }

            void sent(int nbBytes) {
                bytesSent += nbBytes;
                trackOrder.onNext(bytesSent);
                if (!sendBuffer.hasRemaining()) {
                    sendBufferTask.onComplete();
                }
            }

            void error(Throwable throwable) {
                sendBufferTask.onError(throwable);
            }

            void cancel() {
                cancelled = true;
            }

            void doJob() {
                if (cancelled) {
                    currentOrder = null;
                    trackOrder.onError(new Throwable("Order cancelled"));
                    turnOnNIOEvent(SelectionKey.OP_WRITE, null); // check queue
                    return;
                }

                // append an empty element at the end to delay the onComplete()
                job.concatWith(Flowable.just(ByteBuffer.allocate(0)))
                        .subscribe(new DisposableSubscriber<ByteBuffer>() {
                            @Override
                            protected void onStart() {
                                if (cancelled) {
                                    dispose();
                                    onError(new Throwable("cancelled order"));
                                } else {
                                    request(1);
                                }
                            }

                            @Override
                            public void onNext(ByteBuffer byteBuffer) {
                                if (cancelled) {
                                    dispose();
                                    onError(new Throwable("cancelled order"));
                                    return;
                                }

                                if (byteBuffer.remaining() == 0) {
                                    request(1);
                                    return;
                                }

                                Completable.create(t -> {
                                    sendBufferTask = t;
                                    sendBuffer = byteBuffer;
                                    turnOnNIOEvent(SelectionKey.OP_WRITE, null);
                                }).subscribe(
                                        () -> request(1),
                                        e -> { // error sending buffer
                                            dispose();
                                            onError(e);
                                        }
                                );
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                currentOrder = null;
                                trackOrder.onError(throwable);
                                turnOnNIOEvent(SelectionKey.OP_WRITE, null); // will check the queue
                            }

                            @Override
                            public void onComplete() {
                                currentOrder = null;
                                trackOrder.onComplete();
                                turnOnNIOEvent(SelectionKey.OP_WRITE, null); // will check the queue
                            }
                        });
            }
        }

        /**
         * The pipeline for writing data into the socket requires works as follow. It first starts
         * with a call to the order() method that queues a {@see JobOrder} onto the Jobs queue.
         * The queue is a bounded blocking queue and so order() may be blocking if the queue is
         * full. Once the order is added it wakes up the NIO thread to force it to check the queue.
         * <p>
         * <p>After waking up it checks the queue and takes a JobOrder if any. JobOrder is a simple
         * Flowable of ByteBuffer. the NIOTHREAD (thread use for NIO select) subscribes to it in
         * order to observe the ByteBuffer sequence. Since the sending of ByteBuffer is
         * asynchronous, we use request() after every successful transmission to pull the next
         * buffer from the source.
         * <p>
         * <p>When ready_to_write is received, it writes the buffer to the socket and then proceeds
         * in a similar fashion with all the ByteBuffer until it reaches onComplete for this Job,
         * at which point it pulls another Job from the queue (if any) and starts it all again.
         * <p>
         * <pre>
         * MAINTHREAD --------------------------[.]--------------------^-------------------------^-
         *     |                                 |                     |                         |
         *     V                                 |                     |                         |
         * order(JobOrder)                       |                     |                         |
         *     |                                 |                     |                         |
         *  put(JobOrder)                        |                     |                         |
         *     |                             subscribe                 |                         |
         *     |                                 |                     |                         |
         * +---V---+                             |                     |                         |
         * | Queue |>--+--+-----------------     |                     |                         |
         * +---+---+   ^  |                      |                     |                         |
         *     |       |  |                      |                     |                         |
         *  wake up    |  |     +------------+   |                     |                         |
         *     |       |  |  +--| Observable |---V---------------------^-------------------------^-
         *     |       |  |  |  +------------+                         |                         |
         *     |   take() +--+                                  onNext(byteSent)    onNext(byteSent)
         *     |       |  |  |  +----------+                           |                         |
         *     |       |  |  +--| Flowable |--^-----^---Buffer-------- | ----^----Buffer------
         *     |       |  |     +----------+  |     |     |            |     |      |            |
         *     |       |  |                   |  request  |            |  request   |            |
         *     |       |  |              subscribe  |  onNext()        |     |   onNext()        |
         *     V       |  |                   |     |     |            |     |      |            |
         * NIOTHREAD -[.]-V------------------[.]---[.]----V-.//SEND//-[.]---[.]-----V-.//SEND//-[.]
         *                                               / /                       / /
         *       load buffer and turn on write event __ / /        load buffer __ / /
         *                                 write event __/           write event __/
         *
         * </pre>
         */
        private void prepareWritePipeline() throws IOException {
            // write callback
            NIOWriteCallback wc = (key) -> {
                // an order is placed so we can send the buffer
                if ((currentOrder != null) && (currentOrder.sendBuffer != null)
                        && currentOrder.sendBuffer.hasRemaining()) {
                    try {
                        int byteWritten = channel.write(currentOrder.sendBuffer);
                        currentOrder.sent(byteWritten); // update the sendBuffer
                    } catch (IOException io) {
                        currentOrder.error(io);
                        cleanup();
                    }
                    return;
                }

                // no order so we check the queue
                turnOffNIOEvent(SelectionKey.OP_WRITE);
                checkQueue();
            };
            turnOnNIOEvent(SelectionKey.OP_WRITE, wc);
        }

        /**
         * Turn on SelectionKey.READ or SelectionKey.WRITE event listener.
         * If o is not null, it also updates its corresponding callback
         *
         * @param op to turn on
         */
        private void turnOnNIOEvent(int op, Object o) {
            try {
                lock.lock();

                if (channel == null) {
                    return;
                }

                if (key == null) {
                    nio.register(channel, op).subscribe(
                            registeredKey -> {
                                key = registeredKey;
                                if(o != null) {
                                    NIOCallback cb = (NIOCallback)key.attachment();
                                    if(op == SelectionKey.OP_READ) {
                                        cb.r = (NIOReadCallback)o;
                                    } else {
                                        cb.w = (NIOWriteCallback)o;
                                    }
                                }
                            },
                            e -> key = null);
                    return;
                }

                if (key.isValid()) {
                    int ops = key.interestOps();
                    int opsOn = (ops | op);
                    if (ops != opsOn) {
                        key.interestOps(opsOn);
                        if(o != null) {
                            NIOCallback cb = (NIOCallback)key.attachment();
                            if(op == SelectionKey.OP_READ) {
                                cb.r = (NIOReadCallback)o;
                            } else {
                                cb.w = (NIOWriteCallback)o;
                            }
                        }
                        key.selector().wakeup();
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Turn off SelectionKey.READ or SelectionKey.WRITE event listener.
         *
         * @param op to turn off
         */
        private void turnOffNIOEvent(int op) {
            try {
                lock.lock();

                if (key == null) {
                    return;
                }

                if (key.isValid()) {
                    int ops = key.interestOps();
                    int opsOff = (ops & ~op);
                    if (ops != opsOff) {
                        key.interestOps(opsOff);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public String localHost() {
            return channel.socket().getLocalAddress().getHostAddress()
                    + ":" + channel.socket().getLocalPort();
        }
    }
}