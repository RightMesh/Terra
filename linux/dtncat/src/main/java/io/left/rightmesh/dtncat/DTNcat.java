package io.left.rightmesh.dtncat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.GrowingBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.module.aa.ldcp.ActiveLdcpRegistrationCallback;
import io.left.rightmesh.module.aa.ldcp.LdcpApplicationAgent;
import io.reactivex.Completable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "dtncat", mixinStandardHelpOptions = true, version = "dtncat 1.0",
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "",
                "dtncat is a simple Unix utility which reads and writes data across network " +
                        "connections over DTN protocol. dtncat is an application agent and so " +
                        "requires a full DTN node to connect to.",},
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class DTNcat implements Callable<Void> {

    @CommandLine.Parameters(index = "0", description = "connect to the following DTN host.")
    private String dtnhost;

    @CommandLine.Parameters(index = "1", description = "connect to the following DTN host.")
    private int dtnport;

    @CommandLine.Option(names = {"-l", "--listen"}, description = "register to a sink and wait for bundles.")
    private String sink;

    @CommandLine.Option(names = {"-c", "--cookie"}, description = "register to a sink and wait for bundles.")
    private String cookie;

    @CommandLine.Option(names = {"-R", "--report-to"}, description = "report-to Endpoint-ID (EID)")
    private String report;

    @CommandLine.Option(names = {"-L", "--lifetime"}, description = "Lifetime of the bundle")
    private int lifetime;

    @CommandLine.Option(names = {"-D", "--destination"},  description = "Destination Endpoint-ID (EID)")
    private String deid;

    private LdcpApplicationAgent agent;
    private BLOBFactory factory;

    private Bundle createBundleFromSTDIN(Bundle bundle) throws IOException, WritableBLOB.BLOBOverflowException {
        BLOB blob;
        try {
            blob = factory.createBLOB(-1);
        } catch(BLOBFactory.BLOBFactoryException boe) {
            throw new WritableBLOB.BLOBOverflowException();
        }
        WritableBLOB wb = blob.getWritableBLOB();
        InputStream isr = new BufferedInputStream(System.in);
        wb.write(isr);
        wb.close();
        bundle.addBlock(new PayloadBlock(blob));
        System.out.println("size = "+blob.size());
        return bundle;
    }

    private void listenBundle() {
        ActiveLdcpRegistrationCallback cb = (recvbundle) ->
                Completable.create(s -> {
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(System.out);
                        recvbundle.getPayloadBlock().data.getReadableBLOB().read(bos);
                        bos.flush();
                        recvbundle.clearBundle();
                        s.onComplete();
                    } catch (IOException io) {
                        s.onError(io);
                    }
                });

        agent = new LdcpApplicationAgent(dtnhost, dtnport, factory);
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
            agent = new LdcpApplicationAgent(dtnhost, dtnport, factory);
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
            EID destination = EID.create(deid);
            Bundle bundle = new Bundle(destination, lifetime);
            if (report != null) {
                EID reportTo = EID.create(report);
                bundle.reportto = reportTo;
            }

            agent = new LdcpApplicationAgent(dtnhost, dtnport, null);
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
        } catch (IOException | WritableBLOB.BLOBOverflowException | EID.EIDFormatException e) {
            /* ignore */
            System.err.println("error: "+e.getMessage());
        }
    }

    @Override
    public Void call() throws Exception {
        factory = new BaseBLOBFactory().enableVolatile(1000000).enablePersistent("./");
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
