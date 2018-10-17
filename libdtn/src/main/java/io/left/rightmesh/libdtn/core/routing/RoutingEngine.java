package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;
import java.util.NoSuchElementException;

import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.processor.EventListener;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.eid.CLA;
import io.left.rightmesh.libdtn.data.eid.EID;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.ConnectionAgent;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.data.StatusReport.ReasonCode.TransmissionCancelled;


/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {

    static {
        registrations = new HashMap<>();
        listener = new ForwardingListener();
    }

    private static HashMap<String, AARegistrar.RegistrationCallback> registrations;
    private static ForwardingListener listener;

    public static class ForwardingListener extends EventListener<String> {
        @Subscribe
        public void onEvent(ChannelOpened event) {
            /* deliver every bundle of interest */
            getBundlesOfInterest(event.channel.channelEID().getCLASpecificPart()).forEach(
                    bundleID -> {
                        /* retrieve the bundle - should be constant operation */
                        Storage.getMeta(bundleID).subscribe(
                                /* deliver it */
                                bundle -> event.channel.sendBundle(bundle).ignoreElements().subscribe(
                                        () -> {
                                            listener.unwatch(bundle.bid);
                                            BundleProcessor.bundleForwardingSuccessful(bundle);
                                        },
                                        e -> {
                                            bundle.tag("reason_code", TransmissionCancelled);
                                            BundleProcessor.bundleForwardingContraindicated(bundle);
                                        }),
                                e -> {});
                    });
        }
    }

    public static Observable<CLAChannel> findCLA(EID destination) {
        return Observable.concat(
                LinkLocalRouting.findCLA(destination)
                        .toObservable(),
                RoutingTable.resolveEID(destination)
                        .map(LinkLocalRouting::findCLA)
                        .flatMap(m -> m.toObservable()));
    }


    /* not in RFC - store bundle and wait for opportunity */
    //todo remove this part and use EventListener in Routing instead
    public static void forwardLater(final Bundle bundle) {
        /* register a listener that will listen for ChannelOpened event
         * and pull the bundle from storage if there is a match */
        final BundleID bid = bundle.bid;
        final EID destination = bundle.destination;
        Observable<CLA> potentialCLAs = RoutingTable.resolveEID(destination);

        // watch bundle
        potentialCLAs
                .map(claeid -> listener.watch(claeid.getCLASpecificPart(), bid))
                .subscribe();

        // create opportunity
        try {
            potentialCLAs.flatMapMaybe(claeid ->
                    Maybe.fromSingle(ConnectionAgent.createOpportunity(claeid))
                            .onErrorComplete())
                    .blockingFirst();
        } catch(NoSuchElementException nse) {
            /* ignore */
        }
    }


}
