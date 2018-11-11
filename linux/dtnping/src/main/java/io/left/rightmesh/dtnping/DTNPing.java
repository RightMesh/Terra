package io.left.rightmesh.dtnping;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.eid.API;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.module.aa.ldcp.ActiveLdcpRegistrationCallback;
import io.left.rightmesh.module.aa.ldcp.LdcpApplicationAgent;
import io.reactivex.Completable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "dtnping", mixinStandardHelpOptions = true, version = "dtnping 1.0",
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "dtnping - send ping bundle to dtn node", },
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class DTNPing implements Callable<Void> {



    @CommandLine.Parameters(index = "0", description = "connect to the following DTN host.")
    private String dtneid;

    @CommandLine.Option(names = {"-t", "--connect-to"}, description = "connect to DTN daemon host IP address (defaut: localhost)")
    private String dtnhost = "127.0.0.1";

    @CommandLine.Option(names = {"-p", "--port"}, description = "connect to DTN daemon TCP port, (default: 4557)")
    private int dtnport = 4557;

    @CommandLine.Option(names = {"-c", "--cookie"}, description = "cookie to reattach to a previous ping session")
    private String cookie;

    @CommandLine.Option(names = {"-s", "--sessionID"}, description = "manually set the session ID")
    private String sessionID = null;

    private LdcpApplicationAgent agent;
    private String sink;

    private void receiveEchoResponse() {
        ActiveLdcpRegistrationCallback cb = (recvbundle) ->
                Completable.create(s -> {
                    System.err.println("echo response from " + recvbundle.destination.getEIDString());
                    s.onComplete();
                });

        BLOBFactory factory = new BaseBLOBFactory().enableVolatile(10000);
        if(cookie == null) {
            agent = new LdcpApplicationAgent(dtnhost, dtnport, factory);
            agent.register(sink, cb).subscribe(
                    cookie -> {
                        System.err.println("sink registered. cookie: " + cookie);
                    },
                    e -> {
                        System.err.println("could not register to sink: " + sink + " - " + e.getMessage());
                        System.exit(1);
                    });
        } else {
            agent = new LdcpApplicationAgent(dtnhost, dtnport,  factory);
            agent.reAttach(sink, cookie, cb).subscribe(
                    b -> System.err.println("re-attach to registered sink"),
                    e -> {
                        System.err.println("could not re-attach to sink: " + sink + " - " + e.getMessage());
                        System.exit(1);
                    });
        }
    }

    @Override
    public Void call() throws Exception {
        if(sessionID == null) {
            sessionID = Long.toHexString(Double.doubleToLongBits(Math.random()));
        }
        sink = "/dtnping/"+sessionID+"/";

        /* register echo response */
        receiveEchoResponse();

        /* create ping bundle */
        EID destination = EID.create(dtneid+"/null/");
        Bundle bundle = new Bundle(destination);
        bundle.source = API.me();
        bundle.setV7Flag(PrimaryBlock.BundleV7Flags.DELIVERY_REPORT, true);

        AtomicInteger seq = new AtomicInteger(0);
        Runnable sendPing = () -> {
            try {
                /* update ping seq number */
                bundle.reportto = EID.create("api:me" + sink + seq.get());
                agent.send(bundle).subscribe(
                        b -> {
                            if (b) {
                                seq.incrementAndGet();
                            } else {
                                bundle.clearBundle();
                                System.err.println("echo request was refused by " + dtnhost + ":" + dtnport);
                                System.exit(1);
                            }
                        },
                        e -> {
                            bundle.clearBundle();
                            System.err.println("error: " + e.getMessage());
                            System.exit(1);
                        });
            } catch(EID.EIDFormatException efe) {
                /* ignore */
                System.err.println("eid error: "+efe.getMessage());
                System.exit(1);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(sendPing, 0, 1, TimeUnit.SECONDS);

        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new DTNPing(), args);
    }

}
