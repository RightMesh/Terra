package io.left.rightmesh.libdtn.network.cla;

import java.nio.ByteBuffer;
import java.util.Formatter;
import java.util.Queue;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.bundleV6.BundleV6Serializer;
import io.left.rightmesh.libdtn.data.bundleV6.SDNV;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.libdtn.network.DTNChannel;
import io.left.rightmesh.libdtn.network.Peer;
import io.left.rightmesh.libdtn.network.RxTCP;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subscribers.DisposableSubscriber;

/**
 * Simple TCP (STCP) is a TCP Convergence Layer (CL) for the Bundle Protocol. it was introduced in
 * 2018 as an alternative to the quite complicated TCPCLv4. As per the author's own words
 * (Scott Burleigh):
 * <p>
 * <pre>
 *    It is less capable than tcpcl but quite a lot simpler.
 * </pre>
 * <p>
 * <p> An STCP session is unidirectional and bundles flow from the peer that initiated the
 * connection towards the one that passively listen for incoming connection. When the connection
 * is open, bundles can be send without signalling needed. Each bundle is represented as a cbor
 * array with only two item, first item being a cbor integer value representing the length of the
 * serialized bundle followed by the serialized bundle itself. The connection can be shutdown by
 * any peer without any signalling. </p>
 * <p>
 * <p>Mmore details can be read in the draft itself:
 * https://www.ietf.org/id/draft-burleigh-dtn-stcp-00.txt</p>
 *
 * @author Lucien Loiseau on 17/08/18.
 */
public class STCP implements ConvergenceLayer {

    private static final String TAG = "stcp";

    public static class STCPPeer extends TCPPeer {

        public STCPPeer(String host, int port) {
            super(host, port);
        }

        @Override
        public EID getEID() {
            return EID.createCLA("stcp", getTCPAddress());
        }
    }

    public static final int IANA_STCP_PORT_TO_DEFINE = 4557;

    private RxTCP.Server serverDraftSTCP = null;

    public STCP() {
    }

    @Override
    public Observable<DTNChannel> start() {
        return listen(IANA_STCP_PORT_TO_DEFINE);
    }


    @Override
    public void stop() {
        if (serverDraftSTCP != null) {
            serverDraftSTCP.stop();
        }
    }

    public Observable<DTNChannel> listen(int port) {
        return Observable.create(s -> {
            serverDraftSTCP = new RxTCP.Server(port);
            serverDraftSTCP.start()
                    .subscribe(
                            c -> s.onNext(new Channel(c, false)),
                            s::onError,
                            s::onComplete);
        });
    }

    public static Single<DTNChannel> open(Peer peer) {
        if (!(peer instanceof TCPPeer)) {
            return Single.error(new Throwable("Peer is not a TCP Peer"));
        }

        return Single.create(s -> {
            TCPPeer p = (TCPPeer) peer;
            new RxTCP.ConnectionRequest(p.host, p.port)
                    .connect()
                    .subscribe(
                            c -> s.onSuccess(new Channel(c, true)),
                            s::onError);
        });
    }

    public static class Channel implements DTNChannel {

        RxTCP.Connection c;
        EID channelEID;
        boolean initiator;

        /**
         * Constructor.
         *
         * @param c RxTCP underlying connection
         */
        public Channel(RxTCP.Connection c, boolean initiator) {
            this.c = c;
            this.initiator = initiator;

            String remoteAddress = c.channel.socket().getRemoteSocketAddress()
                    .toString().replace("/", "");
            int remotePort = c.channel.socket().getPort();

            try {
                channelEID = EID.create("cla:stcp:tcp//" + remoteAddress + ":" + remotePort);
            } catch (EID.EIDFormatException efe) {
                channelEID = EID.generate();
            }
        }

        @Override
        public EID channelEID() {
            return channelEID;
        }

        @Override
        public void close() {
            c.closeJobsDone();
        }

        @Override
        public Observable<Integer> sendBundle(Bundle bundle) {
            if (!initiator) {
                return Observable.error(new RecvOnlyPeerException());
            }

            Flowable<ByteBuffer> job = createBundleJob(bundle);
            if (job == null) {
                return Observable.error(new Throwable("Cannot serialize the bundle"));
            }

            RxTCP.Connection.JobHandle handle = c.order(job);
            return handle.observe();
        }

        @Override
        public Observable<Integer> sendBundles(Flowable<Bundle> upstream) {
            if (!initiator) {
                return Observable.error(new RecvOnlyPeerException());
            }

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

        public Observable<Bundle> recvBundle() {
            if (initiator) {
                return Observable.empty();
            }

            return Observable.create(s -> {
                CborParser pdu = CBOR.parser()
                        .cbor_open_array(2)
                        .cbor_parse_int((__, ___, i) -> {
                            // we might want check the length and refuse large bundle
                        })
                        .cbor_parse_custom_item(BundleV7Parser.BundleItem::new, (__, ___, item) -> {
                            s.onNext(item.bundle);
                        });

                c.recv().subscribe(
                        buffer -> {
                            try {
                                if (pdu.read(buffer)) {
                                    pdu.reset();
                                }
                            } catch (RxParserException rpe) {
                                s.onComplete();
                                close();
                            }
                        },
                        e -> {
                            s.onComplete();
                            close();
                        },
                        () -> {
                            s.onComplete();
                            close();
                        }
                );
            });
        }

        Flowable<ByteBuffer> createBundleJob(Bundle b) {
            CborEncoder encodedB = BundleV7Serializer.encode(b);
            long[] size = {0};
            encodedB.observe().subscribe(
                    buffer -> {
                        size[0] += buffer.remaining();
                    },
                    e -> {
                        size[0] = -1;
                    });
            if (size[0] < 0) {
                return null;
            }
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(size[0])
                    .merge(encodedB)
                    .observe(2048);
        }

    }

    public static final class SendOnlyPeerException extends Exception {
    }

    public static final class RecvOnlyPeerException extends Exception {
    }
}
