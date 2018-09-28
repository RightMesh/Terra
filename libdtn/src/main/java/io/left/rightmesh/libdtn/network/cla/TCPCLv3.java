package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtn.data.bundleV6.BundleV6Parser;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.bundleV6.BundleV6Serializer;
import io.left.rightmesh.libdtn.data.bundleV6.SDNV;
import io.left.rightmesh.libdtn.network.Peer;
import io.left.rightmesh.libdtn.network.RxTCP;
import io.left.rightmesh.libcbor.rxparser.BufferState;
import io.left.rightmesh.libcbor.rxparser.ParserEmitter;
import io.left.rightmesh.libcbor.rxparser.ParserState;
import io.left.rightmesh.libcbor.rxparser.RxParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libcbor.rxparser.ShortState;
import io.left.rightmesh.libdtn.network.TCPPeer;
import io.left.rightmesh.libdtn.utils.Log;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.subscribers.DisposableSubscriber;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TCPCLv3 is The TCP Convergence Layer (CL) for the Bundle Protocol. When this CL is started, it
 * runs a server on port 4556 and wait for connection. When a new peer connects to the server,
 * it processes the data received following the RFC 7242 and triggers an event for all new Bundle
 * received.
 * <p>
 * <p>TCPCLv3 can also be triggered if Bundles needs to be sent to a peer over a TCP connection in
 * which case a connection is initiated and Bundles are sent following RFC 7242.
 * <p>
 * <p>TCPCLv3 follows the RFC 7242:
 * <p>
 * <pre>
 * +-------------------------+         +-------------------------+
 * |     Contact Header      | ->   <- |     Contact Header      |
 * +-------------------------+         +-------------------------+
 *
 * +-------------------------+
 * |   DATA_SEGMENT (listen)  | ->
 * |    SDNV length [L1]     | ->
 * |  Bundle Data 0..(L1-1)  | ->
 * +-------------------------+
 * +-------------------------+         +-------------------------+
 * |     DATA_SEGMENT        | ->   <- |       ACK_SEGMENT       |
 * |    SDNV length [L2]     | ->   <- |     SDNV length [L1]    |
 * |Bundle Data L1..(L1+L2-1)| ->      +-------------------------+
 * +-------------------------+
 * +-------------------------+         +-------------------------+
 * |    DATA_SEGMENT (end)   | ->   <- |       ACK_SEGMENT       |
 * |     SDNV length [L3]    | ->   <- |   SDNV length [L1+L2]   |
 * |Bundle Data              | ->      +-------------------------+
 * |    (L1+L2)..(L1+L2+L3-1)|
 * +-------------------------+
 *                                     +-------------------------+
 *                                  <- |       ACK_SEGMENT       |
 *                                  <- |  SDNV length [L1+L2+L3] |
 *                                     +-------------------------+
 *
 * +-------------------------+         +-------------------------+
 * |       SHUTDOWN          | ->   <- |         SHUTDOWN        |
 * +-------------------------+         +-------------------------+
 * </pre>
 * <p>
 * <p>The format of the Contact Header is the following:
 * <p>
 * <pre>
 *                     1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +---------------+---------------+---------------+---------------+
 * |                          magic='dtn!'                         |
 * +---------------+---------------+---------------+---------------+
 * |     version   |     parameters     |      keepalive_interval       |
 * +---------------+---------------+---------------+---------------+
 * |                     local EID length (SDNV)                   |
 * +---------------+---------------+---------------+---------------+
 * |                                                               |
 * +                      local EID (variable)                     +
 * |                                                               |
 * +---------------+---------------+---------------+---------------+
 *
 *                 Figure 3: Contact Header Format
 * </pre>
 * <p>
 * <p>Read the <a href="https://tools.ietf.org/html/rfc7242">RFC</a> for details.
 *
 * @author Lucien Loiseau on 17/08/18.
 */
public class TCPCLv3 {

    private static final String TAG = "tcpclv3";

    public static class TCPCLv3Peer extends TCPPeer {
        TCPCLv3Peer(String host, int port) {
            super(host, port);
        }

        @Override
        public EID getEID() {
            return new EID.CLA("tcpclv3", getTCPAddress());
        }
    }

    public static final int IANA_TCPCL_PORT = 4556;

    private RxTCP.Server serverRFC7242;

    private TCPCLv3() {
    }

    /**
     * Starts the TCP convergence layer. A call to listen() will turn on the TCP Server on port 4556
     * and wait for connection. For each connection, it will manage the peer following the
     * RFC7242..
     *
     * @return Observable of CLAChannel
     */
    public Observable<CLAChannel> listen(int port) {
        return Observable.create(s -> {
            serverRFC7242 = new RxTCP.Server(port);
            serverRFC7242.start()
                    .doOnSubscribe(__ -> {
                        Log.d(TAG, "server started.. listenning on 4556..");
                    })
                    .subscribe(
                            c -> {
                                Log.d(TAG, "client connected");
                                s.onNext(new Channel(c));
                            },
                            e -> {
                                Log.d(TAG, "server stopped unexpectedly: "
                                        + e.getMessage());
                            });
        });
    }


    public void stop() {
        serverRFC7242.stop();
    }

    public static Single<CLAChannel> open(Peer peer) {
        if (!(peer instanceof TCPPeer)) {
            return Single.error(new Throwable("Peer is not a TCP Peer"));
        }

        return Single.create(s -> {
            TCPPeer p = (TCPPeer) peer;
            new RxTCP.ConnectionRequest(p.host, p.port)
                    .connect()
                    .subscribe(
                            c -> s.onSuccess(new Channel(c)),
                            e -> s.onError(new Throwable("connection failed"))
                    );
        });
    }

    /**
     * TCP Convergence-Layer Channel (Channel) implements the DTN abstraction of the Bundle
     * protocol over TCP.
     */
    public static class Channel implements CLAChannel {

        RxTCP.Connection c;
        ParserEmitter<ByteBuffer> recvData;
        ContactHeader ch;

        ParameterFlags session_flag;
        int session_interval;

        // only useful if ACK and/or NACK is enabled disabled at the moment
        Queue<RxTCP.Connection.JobHandle> segment_fifo;

        private class ParameterFlags {
            boolean request_ack;
            boolean request_reactive_frag;
            boolean bundle_refusal_support;
            boolean request_sending_length;
            byte flag;

            ParameterFlags(boolean ra, boolean rrf, boolean brs, boolean rsl) {
                request_ack = ra;
                request_reactive_frag = rrf;
                bundle_refusal_support = brs;
                request_sending_length = rsl;
                flag = (byte) ((request_ack ? 0x01 : 0x00)
                        + (request_reactive_frag ? 1 : 0) * 0x02
                        + (bundle_refusal_support ? 1 : 0) * 0x04
                        + (request_sending_length ? 1 : 0) * 0x08);
            }

            ParameterFlags(byte flag) {
                request_ack = ((flag & 0x01) == 0x01);
                request_reactive_frag = ((flag & 0x02) == 0x02);
                bundle_refusal_support = ((flag & 0x04) == 0x04);
                request_sending_length = ((flag & 0x08) == 0x08);
                this.flag = flag;
            }
        }

        /**
         * Constructor.
         *
         * @param c RxTCP underlying connection
         */
        public Channel(RxTCP.Connection c) {
            this.c = c;
            this.segment_fifo = new ConcurrentLinkedQueue<>();

            String remoteAddress = c.channel.socket().getRemoteSocketAddress()
                    .toString().replace("/", "");
            int remotePort = c.channel.socket().getPort();

            EID channelEID;
            try {
                channelEID = EID.create("cla:tcpclv3:tcp//" + remoteAddress + ":" + remotePort);
            } catch (EID.EIDFormatException efe) {
                channelEID = EID.generate();
            }

            ch = new ContactHeader(
                    channelEID,
                    new ParameterFlags(false, false, false, false),
                    0);

            // send contact header
            c.order(createContactHeader())
                    .observe()
                    .subscribe(i -> {
                    }, e -> c.closeNow());
        }

        @Override
        public EID channelEID() {
            return ch.eid;
        }

        @Override
        public void close() {
            c.closeNow();
        }

        @Override
        public Observable<Bundle> recvBundle() {
            return Observable.create(observer ->
                    RxParser.<Bundle>create(
                            RxParser.<ByteBuffer>create(c.recv(),
                                    d -> {
                                        recvData = new TCPCLDataReceiver(d);
                                        return recvData;
                                    }), BundleV6Parser::new)
                            .subscribe(
                                    b -> {
                                        if (!b.isTagged("rejected")) {
                                            observer.onNext(b);
                                        } else {
                                            if (session_flag.bundle_refusal_support) {
                                                c.order(createNACK(NackRCODE.UNSPECIFIED));
                                            }
                                        }
                                    },
                                    observer::onError,
                                    observer::onComplete)
            );
        }

        @Override
        public Observable<Integer> sendBundle(Bundle bundle) {
            RxTCP.Connection.JobHandle handle = c.order(createSegment(bundle));
            return handle.observe();
        }

        @Override
        public Observable<Integer> sendBundles(Flowable<Bundle> upstream) {
            return Observable.create(s -> {
                upstream.subscribe(new DisposableSubscriber<Bundle>() {
                    int bundleSent;

                    @Override
                    protected void onStart() {
                        bundleSent = 0;
                        request(1);
                    }

                    @Override
                    public void onNext(Bundle bundle) {
                        sendBundle(bundle).subscribe(
                                i -> {
                                },
                                e -> request(1),
                                () -> {
                                    s.onNext(++bundleSent);
                                    request(1);
                                }
                        );
                    }

                    @Override
                    public void onError(Throwable t) {
                        s.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        s.onComplete();
                    }
                });
            });
        }

        private class ContactHeader {
            EID eid;
            ParameterFlags parameters;
            int interval;

            ContactHeader(EID eid, ParameterFlags pf, int interval) {
                this.eid = eid;
                this.parameters = pf;
                this.interval = interval;
            }
        }

        private enum MessageType {
            DATA_SEGMENT(0x01),
            ACK_SEGMENT(0x02),
            REFUSE_BUNDLE(0x03),
            KEEPALIVE(0x04),
            SHUTDOWN(0x05),
            LENGTH(0x06);

            byte type;

            MessageType(int type) {
                this.type = (byte) type;
            }

            public byte value() {
                return type;
            }
        }

        private enum NackRCODE {
            UNSPECIFIED(0x00),
            RECEIVED(0x01),
            RESOURCES_EXHAUSTED(0x02),
            RETRANSMIT(0x03);

            byte type;

            NackRCODE(int type) {
                this.type = (byte) type;
            }

            public byte value() {
                return type;
            }
        }

        private enum ShutdownRCODE {
            IDLE_TIMEOUT(0x00),
            VERSION_MISMATCH(0x01),
            BUSY(0x02);

            byte type;

            ShutdownRCODE(int type) {
                this.type = (byte) type;
            }

            public byte value() {
                return type;
            }
        }


        private void onRecvContactHeader(ContactHeader remote) {
            ParameterFlags lp = ch.parameters;
            ParameterFlags rp = remote.parameters;
            session_flag = new ParameterFlags(
                    rp.request_ack & lp.request_ack,
                    rp.request_reactive_frag & lp.request_reactive_frag,
                    rp.bundle_refusal_support & lp.bundle_refusal_support
                            & rp.request_ack & lp.request_ack, lp.request_sending_length);
            session_interval = Math.min(ch.interval, remote.interval);
        }

        private void onRecvAck(long length) {
            // unsupported
        }

        private void onRecvNack(NackRCODE reason) {
            // unsupported
        }

        private void onRecvLength(long length) {
            // unsupported
        }

        private void onRecvKeepAlive() {
            // unsupported
        }

        private void onRecvShutdown() {
            c.closeNow();
        }

        private class TCPCLDataReceiver extends ParserEmitter<ByteBuffer> {
            // protocol mutables
            byte[] magic = new byte[4];
            byte flags;
            int interval;
            long eid_length;
            byte[] eid_array;
            EID eid;

            public TCPCLDataReceiver(Observer<? super ByteBuffer> downstream) {
                super(downstream);
            }

            @Override
            public ParserState initState() {
                return idle;
            }

            @Override
            public void onReset() {
                // should never happen
            }

            // first we receive the contact header
            private ParserState idle = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    return contact_header_magic;
                }
            };

            private ParserState contact_header_magic = new BufferState(magic) {
                @Override
                public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
                    Log.d("TCPCLv3", "magic=" + new String(magic));
                    if (!new String(magic).equals("dtn!")) {
                        throw new RxParserException(new String(magic)
                                + " isn't the magic word");
                    }
                    return contact_header_version;
                }
            };

            private ParserState contact_header_version = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    int version = next.get();
                    Log.d("TCPCLv3", "version=" + version);
                    if (version != 3) {
                        throw new RxParserException("bad TCPCLv3 version ("
                                + version + " != 3)");
                    }
                    return contact_header_flags;
                }
            };

            private ParserState contact_header_flags = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    flags = next.get();
                    Log.d("TCPCLv3", "parameters=" + flags);
                    return contact_header_keepalive_interval;
                }
            };

            private ParserState contact_header_keepalive_interval = new ShortState() {
                @Override
                public ParserState onSuccess(Short s) throws RxParserException {
                    interval = s;
                    Log.d("TCPCLv3", "interval=" + interval);
                    return contact_header_local_eid_length;
                }
            };

            private SDNV.SDNVState contact_header_local_eid_length =
                    new SDNV.SDNVState() {
                        @Override
                        public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
                            eid_length = sdnv_value.getValue();
                            Log.d("TCPCLv3", "eid_length=" + eid_length);
                            contact_header_local_eid.realloc((int) eid_length);
                            return contact_header_local_eid;
                        }
                    };

            private BufferState contact_header_local_eid = new BufferState() {
                @Override
                public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
                    try {
                        eid_array = buffer.array();
                        eid = EID.create(new String(eid_array));
                    } catch (EID.EIDFormatException e) {
                        throw new RxParserException(e.getMessage());
                    }
                    onRecvContactHeader(
                            new ContactHeader(eid, new ParameterFlags(flags), interval));
                    return segment_type;
                }
            };

            // receive actual data
            int code_type;
            int code_flags;
            long segment_length;

            private ParserState segment_type = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    byte code = next.get();
                    code_type = code >> 4;
                    code_flags = code & 0x0F;
                    Log.d("TCPCLv3", "code_type=" + code_type);
                    Log.d("TCPCLv3", "code_flags=" + code_flags);

                    if (code_type == MessageType.DATA_SEGMENT.value()) {
                        return data_segment_length;
                    }
                    if (code_type == MessageType.ACK_SEGMENT.value()) {
                        return ack_segment;
                    }
                    if (code_type == MessageType.REFUSE_BUNDLE.value()) {
                        return refuse_bundle;
                    }
                    if (code_type == MessageType.KEEPALIVE.value()) {
                        return keep_alive;
                    }
                    if (code_type == MessageType.SHUTDOWN.value()) {
                        return shutdown;
                    }
                    if (code_type == MessageType.LENGTH.value()) {
                        return bundle_length;
                    }
                    return this;
                }
            };

            private ParserState data_segment_length = new SDNV.SDNVState() {
                @Override
                public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
                    segment_length = sdnv_value.getValue();
                    Log.d("TCPCLv3", "data_segment_length=" + segment_length);
                    return data_segment_payload;
                }
            };

            private ParserState data_segment_payload = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    int bytes = next.remaining();
                    if (bytes <= segment_length) {
                        segment_length -= bytes;
                        emit(next);
                        if (segment_length == 0) {
                            return segment_type;
                        } else {
                            return this;
                        }
                    } else {

                        // Do not copy the bytebuffer in a new array, that would be wasteful.
                        // Instead we duplicate the bytebuffer (create another pointer) and
                        // manipulate the position and limit.
                        ByteBuffer segment = next.duplicate();
                        segment.limit(segment.position() + (int) segment_length);
                        emit(segment);

                        // since the read() will be done on segment instead of insert, we manually
                        // increase the position
                        next.position(next.position() + (int) segment_length);

                        return segment_type;
                    }
                }
            };

            private ParserState ack_segment = new SDNV.SDNVState() {
                @Override
                public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
                    // sdnv_value is the ack
                    Log.d("TCPCLv3", "ack_segment=" + sdnv_value);
                    return segment_type;
                }
            };

            private ParserState refuse_bundle = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    // nothing to read
                    Log.d("TCPCLv3", "refuse_bundle");
                    return segment_type;
                }
            };


            private ParserState bundle_length = new SDNV.SDNVState() {
                @Override
                public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
                    // sdnv_value is the length of the bundle
                    Log.d("TCPCLv3", "bundle_length=" + bundle_length);
                    return segment_type;
                }
            };

            private ParserState keep_alive = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    // nothing to read, it is just a keep alive
                    Log.d("TCPCLv3", "keep_alive");
                    return segment_type;
                }
            };

            private ParserState shutdown = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    Log.d("TCPCLv3", "shutdown");
                    // R bit set, Read reason code
                    if ((code_flags & 0x02) == 0x02) {
                        return shutdown_reason_code;
                    } else {
                        terminate();
                        return idle;
                    }
                }
            };

            private ParserState shutdown_reason_code = new ParserState() {
                @Override
                public ParserState onNext(ByteBuffer next) throws RxParserException {
                    int reason_code = next.get();
                    Log.d("TCPCLv3", "shutdown_reason_code=" + reason_code);
                    if ((code_flags & 0x01) == 0x01) {
                        return shutdown_reconnection_delay;
                    } else {
                        terminate();
                        return idle;
                    }
                }
            };

            private ParserState shutdown_reconnection_delay = new SDNV.SDNVState() {
                @Override
                public ParserState onSuccess(SDNV sdnv_value) throws RxParserException {
                    // sdnv_value is the reconnection delay
                    Log.d("TCPCLv3", "shutdown_reconnection_delay=" + sdnv_value.getValue());
                    terminate();
                    return idle;
                }
            };
        }

        private static byte[] data_segment_start = {0x12, 0x00};
        private static byte[] data_segment_end = {0x11, 0x00};

        private Flowable<ByteBuffer> createContactHeader() {
            // network byte order two-byte integer
            final byte[] interval = new byte[] {
                    (byte) ((ch.interval >> 8) & 0xFF),
                    (byte) (ch.interval & 0xFF)};
            final byte[] sdnv = new SDNV(ch.eid.toString().getBytes().length).getBytes();
            final byte[] eid = ch.eid.toString().getBytes();
            return Flowable.just(
                    ByteBuffer.allocate(4 + 1 + 1 + 2 + sdnv.length + eid.length)
                            .put("dtn!".getBytes())
                            .put((byte) 0x03)
                            .put(ch.parameters.flag)
                            .put(interval)
                            .put(sdnv)
                            .put(eid));
        }

        // there are no optimization to concatenate small bytebuffer together (like segment start)
        // right now every bytebuffer the source is pushing will trigger at least one TCP packet
        // and so the payload vs TCP overhead is suboptimal. On the plus side, there are no
        // additional copy in a buffer and the bundle is directly serialized on the wire with no
        // intermediate buffer copy.
        //
        // todo optimize that ? that should probably be taken care of at the source
        private Flowable<ByteBuffer> createSegment(Bundle bundle) {
            return Flowable.just(ByteBuffer.wrap(data_segment_start)).concatWith(
                    new BundleV6Serializer().serialize(bundle)
                            .flatMap(buffer -> {
                                final byte[] sdnv = new SDNV(buffer.remaining()).getBytes();
                                return Flowable.just(
                                        // data segment header
                                        ByteBuffer.allocate(1 + sdnv.length)
                                                .put((byte) 0x10)
                                                .put(sdnv),
                                        // data segment payload
                                        buffer);
                            }))
                    .concatWith(Flowable.just(ByteBuffer.wrap(data_segment_end)));
        }

        private Flowable<ByteBuffer> createACK(long length) {
            final byte[] ack = {MessageType.ACK_SEGMENT.value()};
            final byte[] sdnv = new SDNV(length).getBytes();
            return Flowable.just(ByteBuffer.allocate(1 + sdnv.length).put(ack).put(sdnv));
        }

        private Flowable<ByteBuffer> createNACK(NackRCODE reason) {
            final byte[] nack = {(byte) (MessageType.REFUSE_BUNDLE.value() | reason.value())};
            return Flowable.just(ByteBuffer.wrap(nack));
        }

        private Flowable<ByteBuffer> createKeepAlive() {
            final byte[] ka = {MessageType.KEEPALIVE.value()};
            return Flowable.just(ByteBuffer.wrap(ka));
        }

        private Flowable<ByteBuffer> createShutdown(ShutdownRCODE reason, int delay) {
            byte sd = MessageType.SHUTDOWN.value();
            int allocated = 1;

            if (reason != null) {
                sd |= 0x02;
                allocated++;
            }

            final byte[] sdnv = new SDNV(delay).getBytes();
            if (delay > 0) {
                sd |= 0x01;
                allocated += sdnv.length;
            }

            ByteBuffer ret = ByteBuffer.allocate(allocated).put(sd);
            if (reason != null) {
                ret.put(reason.value());
            }
            if (delay > 0) {
                ret.put(sdnv);
            }
            return Flowable.just(ret);
        }

        private Flowable<ByteBuffer> createBundleLength(long length) {
            final byte[] bl = {MessageType.LENGTH.value()};
            final byte[] sdnv = new SDNV(length).getBytes();
            return Flowable.just(ByteBuffer.allocate(1 + sdnv.length).put(bl).put(sdnv));
        }
    }
}
