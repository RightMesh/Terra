package io.left.rightmesh.core.module.cla.stcp;


import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.common.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.utils.NullLogger;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.librxtcp.ConnectionAPI;
import io.left.rightmesh.librxtcp.RxTCP;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subscribers.DisposableSubscriber;

import static io.left.rightmesh.core.module.cla.stcp.Configuration.CLA_STCP_LISTENING_PORT_DEFAULT;
import static io.left.rightmesh.core.module.cla.stcp.Configuration.STCPEntry.CLA_STCP_LISTENING_PORT;

/**
 * Simple TCP (CLASTCP) is a TCP Convergence Layer Adapter (CLA) for the Bundle Protocol. it was
 * introduced by Scott Burleigh in 2018 as an alternative to the quite complicated TCPCLv4.
 * As per the author's own words:
 *
 * <pre>
 *    It is less capable than tcpcl but quite a lot simpler.
 * </pre>
 *
 * <p> An CLASTCP session is unidirectional and bundles flow from the peer that initiated the
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
public class STCP implements ConvergenceLayerSPI {

    private static final String TAG = "STCP";

    private RxTCP.Server<RxTCP.Connection> server;
    private int port = 0;
    private Log logger = new NullLogger();

    public String getModuleName() {
        return "stcp";
    }

    public STCP() {
    }

    public STCP setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public Observable<CLAChannelSPI> start(ConfigurationAPI conf, Log logger) {
        this.logger = logger;
        if (port == 0) {
            port = conf.getModuleConf(this,
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

    private Single<CLAChannelSPI> open(String host, int port) {
        return new RxTCP.ConnectionRequest<>(host, port)
                .connect()
                .map(con -> {
                    CLAChannelSPI channel = new Channel(con, true);
                    return channel;
                });
    }

    public Single<CLAChannelSPI> open(CLA peer) {
        if (peer instanceof CLASTCP) {
            return open(((CLASTCP) peer).host, ((CLASTCP) peer).port);
        } else {
            return Single.error(new Throwable("peer is not a STCP peer"));
        }
    }

    public class Channel implements CLAChannelSPI {

        RxTCP.Connection tcpcon;
        CLA channelEID;
        CLA localEID;
        boolean initiator;
        BundleV7Parser parser;

        /**
         * Constructor.
         *
         * @param initiator true if current node initiated the CLASTCP connection, false otherwise
         */
        public Channel(RxTCP.Connection tcpcon, boolean initiator) {
            this.tcpcon = tcpcon;
            this.initiator = initiator;
            channelEID = CLASTCP.unsafe(tcpcon.getRemoteHost(), tcpcon.getRemotePort());
            localEID = CLASTCP.unsafe(tcpcon.getLocalHost(), tcpcon.getLocalPort());
            logger.i(TAG, "new CLASTCP CLA channel openned (initiated=" + initiator + "): " + channelEID.getEIDString());
        }

        @Override
        public ChannelMode getMode() {
            if (initiator) {
                return ChannelMode.OutUnidirectional;
            } else {
                return ChannelMode.InUnidirectional;
            }
        }

        @Override
        public CLA channelEID() {
            return channelEID;
        }

        @Override
        public CLA localEID() {
            return localEID;
        }

        @Override
        public void close() {
            tcpcon.closeJobsDone();
        }

        @Override
        public Observable<Integer> sendBundle(Bundle bundle) {
            if (!initiator) {
                return Observable.error(new RecvOnlyPeerException());
            }

            // todo move this in caller
            /* pull the bundle from storage if necessary
            if (bundle instanceof MetaBundle) {
                return Observable.create(s -> Storage.get(bundle.bid).subscribe(
                        b -> {
                            Flowable<ByteBuffer> job = createBundleJob(bundle);
                            if (job == null) {
                                s.onError(new Throwable("Cannot serialize the bundle"));
                            }

                            RxTCP.Connection.JobHandle handle = tcpcon.order(job);
                            handle.observe().subscribe(s::onNext);
                        },
                        s::onError));
            } else {
                            */
            Flowable<ByteBuffer> job = createBundleJob(bundle);
            if (job == null) {
                return Observable.error(new Throwable("Cannot serialize the bundle"));
            }

            ConnectionAPI.TrackOrder handle = tcpcon.order(job);
            return handle.observe();
            //}
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

        @Override
        public Observable<Bundle> recvBundle() {
            return recvBundle(new BaseBLOBFactory().disablePersistent());
        }

        @Override
        public Observable<Bundle> recvBundle(BLOBFactory blobFactory) {
            if (initiator) {
                return Observable.create(s ->
                        tcpcon.recv().subscribe(
                                buffer -> {
                                    /* ignore, STCP is unidirectional */
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

            BundleV7Parser parser = new BundleV7Parser(logger, blobFactory);
            return Observable.create(s -> {
                CborParser pdu = CBOR.parser()
                        .cbor_open_array(2)
                        .cbor_parse_int((__, ___, i) -> {
                            // we might want check the length and refuse large bundle
                        })
                        .cbor_parse_custom_item(parser::createBundleItem, (__, ___, item) -> {
                            s.onNext(item.bundle);
                        });

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