package io.left.rightmesh.dtncat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.left.rightmesh.aa.ldcp.api.ActiveRegistrationCallback;
import io.left.rightmesh.aa.ldcp.api.ApplicationAgent;
import io.left.rightmesh.libdtn.common.BaseExtensionToolbox;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.blob.Blob;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.WritableBlob;
import io.left.rightmesh.libdtn.common.data.eid.BaseEidFactory;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.reactivex.Completable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "dtncat", mixinStandardHelpOptions = true, version = "dtncat 1.0",
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "",
                "dtncat is a simple Unix utility which reads and writes data across network " +
                        "connections over DtnEid protocol. dtncat is an application agent and so " +
                        "requires a full DtnEid node to connect to.",},
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class DTNcat implements Callable<Void> {

    @CommandLine.Parameters(index = "0", description = "connect to the following DtnEid host.")
    private String dtnhost;

    @CommandLine.Parameters(index = "1", description = "connect to the following DtnEid host.")
    private int dtnport;

    @CommandLine.Option(names = {"-l", "--listen"}, description = "register to a sink and wait for bundles.")
    private String sink;

    @CommandLine.Option(names = {"-c", "--cookie"}, description = "register to a sink and wait for bundles.")
    private String cookie;

    @CommandLine.Option(names = {"-R", "--report-to"}, description = "report-to Endpoint-ID (Eid)")
    private String report;

    @CommandLine.Option(names = {"-L", "--lifetime"}, description = "Lifetime of the bundle")
    private int lifetime;

    @CommandLine.Option(names = {"-D", "--destination"},  description = "Destination Endpoint-ID (Eid)")
    private String deid;

    @CommandLine.Option(names = {"--crc-16"},  description = "use Crc-16")
    private boolean crc16 = false;

    @CommandLine.Option(names = {"--crc-32"},  description = "use Crc-32")
    private boolean crc32 = false;

    private ApplicationAgent agent;
    private ExtensionToolbox toolbox;
    private BlobFactory factory;

    private Bundle createBundleFromSTDIN(Bundle bundle) throws IOException, WritableBlob.BlobOverflowException {
        Blob blob;
        try {
            blob = factory.createBlob(-1);
        } catch(BlobFactory.BlobFactoryException boe) {
            throw new WritableBlob.BlobOverflowException();
        }
        WritableBlob wb = blob.getWritableBlob();
        InputStream isr = new BufferedInputStream(System.in);
        wb.write(isr);
        wb.close();
        bundle.addBlock(new PayloadBlock(blob));

        if(crc16) {
            bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_16);
            bundle.getPayloadBlock().crcType = BlockHeader.CrcFieldType.CRC_16;
        }
        if(crc32) {
            bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_32);
            bundle.getPayloadBlock().crcType = BlockHeader.CrcFieldType.CRC_32;
        }

        return bundle;
    }

    private void listenBundle() {
        ActiveRegistrationCallback cb = (recvbundle) ->
                Completable.create(s -> {
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(System.out);
                        recvbundle.getPayloadBlock().data.observe().subscribe(
                                byteBuffer -> {
                                    while(byteBuffer.hasRemaining()) {
                                        bos.write(byteBuffer.get());
                                    }
                                },
                                e -> {
                                    /* ignore */
                                }
                        );
                        bos.flush();
                        recvbundle.clearBundle();
                        s.onComplete();
                    } catch (IOException io) {
                        s.onError(io);
                    }
                });

        agent = new ApplicationAgent(dtnhost, dtnport, toolbox, factory);
        if(cookie == null) {
            agent.register(sink, cb).subscribe(
                    cookie -> {
                        System.err.println("sink registered. cookie: " + cookie);
                    },
                    e -> {
                        System.err.println("could not register to sink: " + sink + " - " + e.getMessage());
                        System.exit(1);
                    });
        } else {
            agent = new ApplicationAgent(dtnhost, dtnport, toolbox, factory);
            agent.reAttach(sink, cookie, cb).subscribe(
                    b -> System.err.println("re-attach to registered sink"),
                    e -> {
                        System.err.println("could not re-attach to sink: " + sink + " - " + e.getMessage());
                        System.exit(1);
                    });
        }
    }

    private void sendBundle() {
        try {
            if(deid == null) {
                throw new IOException("destination must be set");
            }
            Eid destination = new BaseEidFactory().create(deid);
            Bundle bundle = new Bundle(destination, lifetime);
            if (report != null) {
                Eid reportTo = new BaseEidFactory().create(report);
                bundle.setReportto(reportTo);
            }

            agent = new ApplicationAgent(dtnhost, dtnport, toolbox, null);
            agent.send(createBundleFromSTDIN(bundle)).subscribe(
                    b -> {
                        if (b) {
                            bundle.clearBundle();
                            System.err.println("bundle successfully sent to " + dtnhost + ":" + dtnport);
                            System.exit(0);
                        } else {
                            bundle.clearBundle();
                            System.err.println("bundle was refused by " + dtnhost + ":" + dtnport);
                            System.exit(1);
                        }
                    },
                    e -> {
                        bundle.clearBundle();
                        System.err.println("error: " + e.getMessage());
                        System.exit(1);
                    });
        } catch (IOException | WritableBlob.BlobOverflowException | EidFormatException e) {
            /* ignore */
            System.err.println("error: "+e.getMessage());
        }
    }

    @Override
    public Void call() throws Exception {
        toolbox = new BaseExtensionToolbox();
        factory = new BaseBlobFactory().enableVolatile(1000000).enablePersistent("./");
        if(sink != null) {
            listenBundle();
        } else {
            sendBundle();
        }
        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new DTNcat(), args);
    }

}
