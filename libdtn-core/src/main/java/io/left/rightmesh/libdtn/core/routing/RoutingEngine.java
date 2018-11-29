package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.eid.BaseCLAEID;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.RoutingAPI;
import io.left.rightmesh.libdtn.core.storage.EventListener;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.TransmissionCancelled;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine implements RoutingAPI {

    public static final String TAG = "RoutingEngine";

    private CoreAPI core;

    public RoutingEngine(CoreAPI core) {
        this.core = core;
        listener = new ForwardingListener(core);
    }

    private ForwardingListener listener;

    @Override
    public void addRoute(EID to, EID nextHop) {
        core.getLogger().i(TAG, "adding a new Route: " + to.getEIDString() + " -> " + nextHop.getEIDString());
        core.getRoutingTable().addRoute(to, nextHop);
    }

    public class ForwardingListener extends EventListener<String> {
        ForwardingListener(CoreAPI core) {
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
                        /* retrieve the bundle */
                        core.getStorage().get(bundleID).subscribe(
                                /* deliver it */
                                bundle -> event.channel.sendBundle(
                                        bundle,
                                        core.getExtensionManager().getBlockDataSerializerFactory()
                                ).ignoreElements().subscribe(
                                        () -> {
                                            listener.unwatch(bundle.bid);
                                            core.getBundleProcessor()
                                                    .bundleForwardingSuccessful(bundle);
                                        },
                                        e -> {
                                            bundle.tag("reason_code", TransmissionCancelled);
                                            core.getBundleProcessor()
                                                    .bundleForwardingContraindicated(bundle);
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
                        .flatMap(Maybe::toObservable))
                .distinct();
    }

    /* not in RFC - store bundle and wait for an opportunity */
    public void forwardLater(final Bundle bundle) {
        /* register a listener that will listen for ChannelOpened event
         * and pull the bundle from storage if there is a match */
        final BundleID bid = bundle.bid;
        final EID destination = bundle.destination;
        Observable<BaseCLAEID> potentialCLAs = core.getRoutingTable().resolveEID(destination);

        // watch bundle for all potential BaseCLAEID
        potentialCLAs
                .map(claeid -> listener.watch(claeid.getCLASpecificPart(), bid))
                .subscribe();

        // then try to force an opportunity
        potentialCLAs
                .distinct()
                .concatMapMaybe(claeid ->
                        Maybe.fromSingle(core.getClaManager()
                                .createOpportunity(claeid))
                                .onErrorComplete())
                .firstElement()
                .subscribe(
                        (channel) -> {
                            /* ignore - bundle listener will take care of forwarding it*/
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
            String local = entry.localEID().getEIDString();
            String mode;
            if (entry.getMode().equals(CLAChannelSPI.ChannelMode.InUnidirectional)) {
                mode = " <-- ";
            } else if (entry.getMode().equals(CLAChannelSPI.ChannelMode.OutUnidirectional)) {
                mode = " --> ";
            } else {
                mode = " <-> ";
            }
            sb.append(local + mode + remote + "\n");
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
                    sb.append(tableEntry.getTo().getEIDString() + " --> " + tableEntry.getNext().getEIDString() + "\n");
                }
        );
        sb.append("\n");
        return sb.toString();
    }
}
