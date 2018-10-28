package io.left.rightmesh.dtncat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.module.aa.ldcp.ActiveLdcpRegistrationCallback;
import io.left.rightmesh.module.aa.ldcp.LdcpApplicationAgent;
import io.left.rightmesh.module.aa.ldcp.LdcpRequest;
import io.reactivex.Completable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "dtncat", mixinStandardHelpOptions = true, version = "dtncat 1.0",
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "dtncat is a simple Unix utility which reads and writes data across network " +
                        "connections over DTN protocol. dtncat is an application agent and so " +
                        "requires a full DTN node to connect to.",},
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class DTNcat implements Callable<Void> {

    @CommandLine.Option(names = {"-c", "--connect"}, description = "connect to the following DTN host.")
    private String dtnhost;

    @CommandLine.Option(names = {"-p", "--port"}, description = "connect to the following DTN host.")
    private int dtnport;

    @CommandLine.Option(names = {"-r", "--report-to"}, description = "report-to Endpoint-ID (EID)")
    private String report;

    @CommandLine.Option(names = {"-l", "--listen"}, description = "listen to bundles.")
    private String sink;

    @CommandLine.Parameters(arity = "1", paramLabel = "destinationEID", description = "Destination Endpoint-ID (EID)")
    private String deid;

    private LdcpApplicationAgent agent;

    private Bundle createBundleFromSTDIN() {
        Bundle bundle = new Bundle();
        try {
            BLOB blob = new ByteBufferBLOB(20000);
            WritableBLOB wb = blob.getWritableBLOB();
            InputStream isr = new BufferedInputStream(System.in);
            wb.write(isr);
            wb.close();
            bundle.addBlock(new PayloadBlock(blob));
        } catch (IOException | WritableBLOB.BLOBOverflowException e) {
            /* ignore */
        }
        return bundle;
    }

    @Override
    public Void call() throws Exception {
        if (sink != null) {
            agent = new LdcpApplicationAgent(dtnhost, dtnport, new BaseBLOBFactory().disablePersistent());
            agent.register("/dtncat/", (bundle) ->
                    Completable.create(s -> {
                        System.err.println("bundle received: " + bundle.toString());
                        try {
                            bundle.getPayloadBlock().data.getReadableBLOB().read(new BufferedOutputStream(System.out));
                        } catch (IOException io) {
                            /* ignore */
                        }
                    })
            ).subscribe(
                    cookie -> {
                        System.err.println("sink registered. cookie: " + cookie);
                    },
                    e -> {
                        System.err.println("could not register to sink: " + sink + " error: " + e.getMessage());
                    });
        } else {
            agent = new LdcpApplicationAgent(dtnhost, dtnport, null);
            agent.send(createBundleFromSTDIN()).subscribe(
                    b -> {
                        if (b) {
                            System.out.println("bundle successfully sent to " + dtnhost + ":" + dtnport);
                        } else {
                            System.out.println("bundle was refused by " + dtnhost + ":" + dtnport);
                        }
                    },
                    e -> System.out.println("error: " + e.getMessage()));
        }
        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new DTNcat(), args);
    }

}
