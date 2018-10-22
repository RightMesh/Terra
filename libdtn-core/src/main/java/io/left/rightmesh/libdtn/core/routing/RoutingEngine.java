package io.left.rightmesh.libdtn.core.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.processor.EventListener;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.network.ConnectionAgent;
import io.left.rightmesh.libdtn.modules.cla.CLAChannel;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.TransmissionCancelled;


/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {

    private DTNCore core;

    public RoutingEngine(DTNCore core) {
        this.core = core;
        registrations = new ConcurrentHashMap<>();
        listener = new ForwardingListener(core);
    }

    private Map<String, AARegistrar.RegistrationCallback> registrations;
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

    public Observable<CLAChannel> findCLA(EID destination) {
        return Observable.concat(
                core.getLinkLocalRouting().findCLA(destination)
                        .toObservable(),
                core.getRoutingTable().resolveEID(destination)
                        .map(core.getLinkLocalRouting()::findCLA)
                        .flatMap(m -> m.toObservable()));
    }


    /* not in RFC - store bundle and wait for opportunity */
    //todo remove this part and use EventListener in Routing instead
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
}
