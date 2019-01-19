package io.left.rightmesh.libdtn.core.routing;

import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.TransmissionCancelled;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.RoutingApi;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.left.rightmesh.libdtn.core.storage.EventListener;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

/**
 * RoutingEngine forward bundles to next-hop connected neighbour.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine implements RoutingApi {

    public static final String TAG = "RoutingEngine";

    private CoreApi core;

    public RoutingEngine(CoreApi core) {
        this.core = core;
        listener = new ForwardingListener(core);
    }

    private ForwardingListener listener;

    public class ForwardingListener extends EventListener<String> {
        ForwardingListener(CoreApi core) {
            super(core);
        }

        @Override
        public String getComponentName() {
            return "ForwardingListener";
        }

        /**
         * Listen for new peer event and forward relevent bundle accordingly.
         *
         * @param event new peer
         */
        // CHECKSTYLE IGNORE LineLength
        @Subscribe
        public void onEvent(LinkLocalEntryUp event) {
            /* deliver every bundle of interest */
            core.getLogger().i(TAG, "step 1: pull bundleOfInterest key="
                    + event.channel.channelEid().getClaSpecificPart());
            getBundlesOfInterest(event.channel.channelEid().getClaSpecificPart()).subscribe(
                    bundleID -> {
                        /* retrieve the bundle */
                        core.getLogger().v(TAG, "step 1.1: pull from storage "
                                + bundleID.getBidString());
                        core.getStorage().get(bundleID).subscribe(
                                /* deliver it */
                                bundle -> {
                                    core.getLogger().v(TAG, "step 1.2-1: forward bundle "
                                            + bundleID.getBidString());
                                    event.channel.sendBundle(
                                            bundle,
                                            core.getExtensionManager().getBlockDataSerializerFactory()
                                    ).ignoreElements().subscribe(
                                            () -> {
                                                core.getLogger().v(TAG, "step 1.3: forward successful, resume processing " + bundleID.getBidString());
                                                listener.unwatch(bundle.bid);
                                                core.getBundleProcessor()
                                                        .bundleForwardingSuccessful(bundle);
                                            },
                                            e -> {
                                                core.getLogger().v(TAG, "step 1.3: forward failed, resume processing " + bundleID.getBidString());
                                                bundle.tag("reason_code", TransmissionCancelled);
                                                core.getBundleProcessor()
                                                        .bundleForwardingContraindicated(bundle);
                                            });
                                },
                                e -> {
                                    core.getLogger().w(TAG, "step 1.2-2: failed to pull bundle from storage " + bundleID.getBidString());
                                });
                    });
        }
        // CHECKSTYLE END IGNORE LineLength
    }

    /**
     * finds all the channels, actually opened, that are next-hop toward a certain destination.
     *
     * @param destination endpoint if of the destination
     * @return an Observable of openned {@link ClaChannelSpi}
     */
    public Observable<ClaChannelSpi> findOpenedChannelTowards(Eid destination) {
        return Observable.concat(
                core.getLinkLocalRouting().findCla(destination)
                        .toObservable(),
                core.getRoutingTable().resolveEid(destination)
                        .map(core.getLinkLocalRouting()::findCla)
                        .flatMap(Maybe::toObservable))
                .distinct();
    }

    /**
     * This method will track the event related to this bundle and tries to forward it later if
     * an opportunity happened.
     *
     * @param bundle to forward later
     */
    public void forwardLater(final Bundle bundle) {
        /* register a listener that will listen for ChannelOpened event
         * and pull the bundle from storage if there is a match */
        final BundleId bid = bundle.bid;
        final Eid destination = bundle.getDestination();
        Observable<BaseClaEid> potentialClas = core.getRoutingTable().resolveEid(destination);

        // watch bundle for all potential BaseClaEid
        potentialClas
                .map(claeid -> listener.watch(claeid.getClaSpecificPart(), bid))
                .subscribe();

        // then try to force an opportunity
        potentialClas
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
                        });
    }


    /**
     * dump the link-local table.
     * @return human readable link-local table
     */
    public String printLinkLocalTable() {
        StringBuilder sb = new StringBuilder("Link-Local Table:\n");
        sb.append("--------------\n\n");
        core.getLinkLocalRouting().dumpTable().forEach((entry) -> {
            String remote = entry.channelEid().getEidString();
            String local = entry.localEid().getEidString();
            String mode;
            if (entry.getMode().equals(ClaChannelSpi.ChannelMode.InUnidirectional)) {
                mode = " <-- ";
            } else if (entry.getMode().equals(ClaChannelSpi.ChannelMode.OutUnidirectional)) {
                mode = " --> ";
            } else {
                mode = " <-> ";
            }
            sb.append(local + mode + remote + "\n");
        });
        sb.append("\n");
        return sb.toString();
    }

    /**
     * dump the routing table.
     * @return human readable routing table
     */
    public String printRoutingTable() {
        final StringBuilder sb = new StringBuilder("Routing Table:\n");
        sb.append("--------------\n\n");
        core.getRoutingTable().dumpTable().forEach(
                tableEntry -> {
                    sb.append(tableEntry.getTo().getEidString() + " --> "
                            + tableEntry.getNext().getEidString() + "\n");
                }
        );
        sb.append("\n");
        return sb.toString();
    }
}
