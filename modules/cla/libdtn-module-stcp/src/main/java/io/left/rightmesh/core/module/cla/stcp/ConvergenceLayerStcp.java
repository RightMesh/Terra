package io.left.rightmesh.core.module.cla.stcp;

import static io.left.rightmesh.core.module.cla.stcp.Configuration.CLA_STCP_LISTENING_PORT;
import static io.left.rightmesh.core.module.cla.stcp.Configuration.CLA_STCP_LISTENING_PORT_DEFAULT;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSpi;
import io.left.rightmesh.librxtcp.ConnectionAPI;
import io.left.rightmesh.librxtcp.RxTCP;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import java.nio.ByteBuffer;

/**
 * Simple TCP (ClaStcpEid) is a TCP Convergence Layer Adapter (BaseClaEid) for the Bundle Protocol.
 * it was introduced by Scott Burleigh in 2018 as an alternative to the quite complicated TCPCLv4.
 * As per the author's own words:
 *
 * <pre>
 *    It is less capable than tcpcl but quite a lot simpler.
 * </pre>
 *
 * <p> An ClaStcpEid session is unidirectional and bundles flow from the peer that initiated the
 * connection towards the one that passively listen for incoming connection. When the connection
 * is open, bundles can be send without signalling needed. Each bundle is represented as a cbor
 * array with only two items, first item being a cbor integer value representing the length of the
 * serialized bundle followed by the serialized bundle itself. The connection can be shutdown by
 * any peer without any signalling needed. </p>
 *
 * <p>More details can be read in the draft itself:
 * https://www.ietf.org/id/draft-burleigh-dtn-stcp-00.txt</p>
 *
 * @author Lucien Loiseau on 17/08/18.
 */
public class ConvergenceLayerStcp implements ConvergenceLayerSpi {

    private static final String TAG = "ConvergenceLayerStcp";

    private RxTCP.Server<RxTCP.Connection> server;
    private int port = 0;
    private Log logger = new NullLogger();

    public String getModuleName() {
        return "stcp";
    }

    /**
     * Default constructor.
     */
    public ConvergenceLayerStcp() {
    }

    /**
     * set the remote tcp port for the STCP connection.
     *
     * @param port tcp port
     * @return this ConvergenceLayerStcp instance
     */
    public ConvergenceLayerStcp setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ClaEidParser getClaEidParser() {
        return new ClaStcpEidParser();
    }

    @Override
    public Observable<ClaChannelSpi> start(ConfigurationApi conf, Log logger) {
        this.logger = logger;
        if (port == 0) {
            port = conf.getModuleConf(getModuleName(),
                    CLA_STCP_LISTENING_PORT, CLA_STCP_LISTENING_PORT_DEFAULT).value();
        }
        server = new RxTCP.Server<>(port);
        logger.i(TAG, "starting a stcp server on port " + port);
        return server.start()
                .map(tcpcon -> new Channel(tcpcon, false));
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private Single<ClaChannelSpi> open(String host, int port) {
        return new RxTCP.ConnectionRequest<>(host, port)
                .connect()
                .map(con -> {
                    ClaChannelSpi channel = new Channel(con, true);
                    return channel;
                });
    }

    @Override
    public Single<ClaChannelSpi> open(ClaEid peer) {
        if (peer instanceof ClaStcpEid) {
            return open(((ClaStcpEid) peer).host, ((ClaStcpEid) peer).port);
        } else {
            return Single.error(new Throwable("peer is not a ConvergenceLayerStcp peer"));
        }
    }

    public class Channel implements ClaChannelSpi {

        RxTCP.Connection tcpcon;
        BaseClaEid channelEid;
        BaseClaEid localEid;
        boolean initiator;

        /**
         * Constructor.
         *
         * @param tcpcon    tcp connection
         * @param initiator true if current node initiated the connection, false otherwise
         */
        public Channel(RxTCP.Connection tcpcon, boolean initiator) {
            this.tcpcon = tcpcon;
            this.initiator = initiator;
            channelEid = ClaStcpEid.unsafe(tcpcon.getRemoteHost(), tcpcon.getRemotePort());
            localEid = ClaStcpEid.unsafe(tcpcon.getLocalHost(), tcpcon.getLocalPort());
            logger.i(TAG, "new ClaStcpEid CLA channel openned (initiated="
                    + initiator + "): " + channelEid.getEidString());
        }

        @Override
        public ChannelMode getMode() {
            /*
            if (initiator) {
                return ChannelMode.OutUnidirectional;
            } else {
                return ChannelMode.InUnidirectional;
            }
            */
            return ChannelMode.BiDirectional;
        }

        @Override
        public BaseClaEid channelEid() {
            return channelEid;
        }

        @Override
        public BaseClaEid localEid() {
            return localEid;
        }

        @Override
        public void close() {
            tcpcon.closeJobsDone();
        }

        @Override
        public Observable<Integer> sendBundle(Bundle bundle,
                                              BlockDataSerializerFactory serializerFactory) {
            /*
            if (!initiator) {
                return Observable.error(new RecvOnlyPeerException());
            }
            */
            Flowable<ByteBuffer> job = createBundleJob(bundle, serializerFactory);
            if (job == null) {
                return Observable.error(new Throwable("Cannot serialize the bundle"));
            }

            ConnectionAPI.TrackOrder handle = tcpcon.order(job);
            return handle.track();
        }


        @Override
        public Observable<Integer> sendBundles(Flowable<Bundle> upstream,
                                               BlockDataSerializerFactory serializerFactory) {
            /*
            if (!initiator) {
                return Observable.error(new RecvOnlyPeerException());
            }
            */
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
                        sendBundle(bundle, serializerFactory).subscribe(
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

        @Override
        public Observable<Bundle> recvBundle(ExtensionToolbox toolbox,
                                             BlobFactory blobFactory) {
            /*
            if (initiator) {
                return Observable.create(s ->
                        tcpcon.recv().subscribe(
                                buffer -> {

                                },
                                e -> {
                                    s.onComplete();
                                    close();
                                },
                                () -> {
                                    s.onComplete();
                                    close();
                                }));
            }
            */

            return Observable.<Bundle>create(s -> {
                CborParser pdu = CBOR.parser()
                        .cbor_open_array(2)
                        .cbor_parse_int((p, t, i) -> {
                            // we might want check the length and refuse large bundle
                        })
                        .cbor_parse_custom_item(
                                () -> new BundleV7Item(
                                        logger,
                                        toolbox,
                                        blobFactory),
                                (p, t, item) -> s.onNext(item.bundle));

                tcpcon.recv().subscribe(
                        buffer -> {
                            try {
                                while (buffer.hasRemaining()) {
                                    if (pdu.read(buffer)) {
                                        pdu.reset();
                                    }
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
            }).observeOn(Schedulers.io());
        }

        Flowable<ByteBuffer> createBundleJob(Bundle b,
                                             BlockDataSerializerFactory serializerFactory) {
            CborEncoder encodedB = BundleV7Serializer.encode(b, serializerFactory);
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