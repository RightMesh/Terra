package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.api.RoutingAPI;
import io.left.rightmesh.libdtn.core.processor.EventListener;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.TransmissionCancelled;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine implements RoutingAPI {

    private DTNCore core;

    public RoutingEngine(DTNCore core) {
        this.core = core;
        listener = new ForwardingListener(core);
    }

    private ForwardingListener listener;

    public class ForwardingListener extends EventListener<String> {
        ForwardingListener(DTNCore core) {
            super(core);
        }

        @Override
        public String getComponentName() {
            return "ForwardingListener";
        }

        @Subscribe
        public void onEvent(LinkLocalEntryUp event) {
            /* deliver every bundle of interest */
            getBundlesOfInterest(event.channel.channelEID().getCLASpecificPart()).subscribe(
                    bundleID -> {
                        /* retrieve the bundle - should be constant operation */
                        core.getStorage().getMeta(bundleID).subscribe(
                                /* deliver it */
                                bundle -> event.channel.sendBundle(bundle).ignoreElements().subscribe(
                                        () -> {
                                            listener.unwatch(bundle.bid);
                                            core.getBundleProcessor().bundleForwardingSuccessful(bundle);
                                        },
                                        e -> {
                                            bundle.tag("reason_code", TransmissionCancelled);
                                            core.getBundleProcessor().bundleForwardingContraindicated(bundle);
                                        }),
                                e -> { /* should we delete it ? */});
                    });
        }
    }

    public Observable<CLAChannelSPI> findOpenedChannelTowards(EID destination) {
        return Observable.concat(
                core.getLinkLocalRouting().findCLA(destination)
                        .toObservable(),
                core.getRoutingTable().resolveEID(destination)
                        .map(core.getLinkLocalRouting()::findCLA)
                        .flatMap(m -> m.toObservable()))
                .distinct();
    }


    /* not in RFC - store bundle and wait for an opportunity */
    public void forwardLater(final Bundle bundle) {
        /* register a listener that will listen for ChannelOpened event
         * and pull the bundle from storage if there is a match */
        final BundleID bid = bundle.bid;
        final EID destination = bundle.destination;
        Observable<CLA> potentialCLAs = core.getRoutingTable().resolveEID(destination);

        // watch bundle for all potential CLA
        potentialCLAs
                .map(claeid -> listener.watch(claeid.getCLASpecificPart(), bid))
                .subscribe();

        potentialCLAs
                .distinct()
                .flatMapMaybe(claeid -> {
                    System.out.println(" eid -> "+claeid.getEIDString());
                    return Maybe.fromSingle(core.getConnectionAgent().createOpportunityForBundle(claeid))
                            .onErrorComplete();
                })
                .firstElement()
                .subscribe(
                        (channel) -> {
                            RxBus.post(new ChannelOpened(channel));
                            channel.recvBundle().ignoreElements().subscribe(
                                    () -> RxBus.post(new ChannelClosed(channel)),
                                    e -> RxBus.post(new ChannelClosed(channel)));
                        },
                        e -> {
                            /* ignore */
                        },
                        () -> {
                            /* ignore */
                        }
                );
    }


    // todo remove this
    public String printLinkLocalTable() {
        StringBuilder sb = new StringBuilder("Link-Local Table:\n");
        sb.append("--------------\n\n");
        core.getLinkLocalRouting().dumpTable().forEach((entry) -> {
            String remote = entry.channelEID().getEIDString();
            String local  = entry.localEID().getEIDString();
            String mode;
            if(entry.getMode().equals(CLAChannelSPI.ChannelMode.InUnidirectional)) {
                mode = " <-- ";
            } else if (entry.getMode().equals(CLAChannelSPI.ChannelMode.OutUnidirectional)) {
                mode = " --> ";
            } else {
                mode = " <-> ";
            }
            sb.append(local + mode +remote+"\n");
        });
        sb.append("\n");
        return sb.toString();
    }


    // todo remove this
    public String printRoutingTable() {
        final StringBuilder sb = new StringBuilder("Routing Table:\n");
        sb.append("--------------\n\n");
        core.getRoutingTable().dumpTable().forEach(
                tableEntry -> {
                    sb.append(tableEntry.getTo().getEIDString() + " --> "+tableEntry.getNext().getEIDString()+"\n");
                }
        );
        sb.append("\n");
        return sb.toString();
    }
}
