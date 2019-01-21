package io.left.rightmesh.libdtn.core.routing.strategies.direct;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.DirectRoutingStrategyApi;
import io.left.rightmesh.libdtn.core.api.EventListenerApi;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * DirectRoutingStrategy will try to forward a bundle using a directly connected peer
 * after resolving the destination Eid against the link-local table and main routing table.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public class DirectRoutingStrategy implements DirectRoutingStrategyApi {

    private static final String TAG = "DirectRouting";
    public static final int MAIN_ROUTING_STRATEGY_ID = 1;

    private CoreApi core;
    private EventListenerApi directListener;

    public DirectRoutingStrategy(CoreApi core, EventListenerApi directListener) {
        this.core = core;
        this.directListener = directListener;
    }

    @Override
    public int getRoutingStrategyId() {
        return MAIN_ROUTING_STRATEGY_ID;
    }

    @Override
    public String getRoutingStrategyName() {
        return TAG;
    }

    @Override
    public Single<RoutingStrategyResult> route(Bundle bundle) {
        return findOpenedChannelTowards(bundle.getDestination())
                .concatMapMaybe(
                        claChannel ->
                                claChannel.sendBundle(
                                        bundle,
                                        core.getExtensionManager().getBlockDataSerializerFactory())
                                        .doOnSubscribe(
                                                (disposable) ->
                                                        prepareBundleForTransmission(
                                                                bundle,
                                                                claChannel))
                                        .lastElement()
                                        .onErrorComplete())
                .map(byteSent -> RoutingStrategyResult.Forwarded)
                .firstElement()
                .toSingle(RoutingStrategyResult.CustodyRefused);
    }

    private Observable<ClaChannelSpi> findOpenedChannelTowards(Eid destination) {
        return Observable.concat(
                core.getLinkLocalTable().findCla(destination)
                        .toObservable(),
                core.getRoutingTable().resolveEid(destination)
                        .map(core.getLinkLocalTable()::findCla)
                        .flatMap(Maybe::toObservable))
                .distinct();
    }

    private void prepareBundleForTransmission(Bundle bundle, ClaChannelSpi claChannel) {
        core.getLogger().v(TAG, "5.4-4 "
                + bundle.bid.getBidString() + " -> "
                + claChannel.channelEid().getEidString());

        /* call block-specific routine for transmission */
        for (CanonicalBlock block : bundle.getBlocks()) {
            try {
                core.getExtensionManager()
                        .getBlockProcessorFactory()
                        .create(block.type)
                        .onPrepareForTransmission(
                                block,
                                bundle,
                                core.getLogger());
            } catch (ProcessingException | BlockProcessorFactory.ProcessorNotFoundException pe) {
                /* ignore */
            }
        }
    }

    @Override
    public Single<RoutingStrategyResult> routeLater(final Bundle bundle) {
        if (!bundle.isTagged("in_storage")) {
            return core.getStorage()
                    .store(bundle)
                    .flatMap(this::forwardLater);
        } else {
            return forwardLater(bundle);
        }
    }

    private Single<RoutingStrategyResult> forwardLater(Bundle bundle) {
        /* register a listener that will listen for LinkLocalEntryUp event
         * and pull the bundle from storage if there is a match */
        final BundleId bid = bundle.bid;
        final Eid destination = bundle.getDestination();
        Observable<BaseClaEid> potentialClas = core.getRoutingTable().resolveEid(destination);

        // watch bundle for all potential ClaEid
        potentialClas
                .map(claeid -> directListener.watch(claeid.getClaSpecificPart(), bid))
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
                            /* ignore - directListener will take care of forwarding it*/
                        },
                        e -> {
                            /* ignore */
                        },
                        () -> {
                            /* ignore */
                        });

        return Single.just(RoutingStrategyResult.CustodyAccepted);
    }

}
