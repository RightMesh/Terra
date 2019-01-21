package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.RoutingBlock;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.DirectRoutingStrategyApi;
import io.left.rightmesh.libdtn.core.api.RoutingEngineApi;
import io.left.rightmesh.libdtn.core.api.RoutingStrategyApi;
import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;

/**
 * RoutingEngine is a meta-routing strategy. It first invoke the service of the
 * direct routing strategy that will try to use a next hop that is directly available over one
 * of the connected channel (if any). If no next-hop are available, routing engine will choose the
 * appropriate routing strategy (using the RoutingBlock in the bundle) and invoke its services
 * if such strategy is found. Otherwise, it falls back on direct routing listener that will register
 * the bundle for futur delivery and listens for network opportunity.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public class RoutingEngine implements RoutingEngineApi {

    private static final String TAG = "RoutingEngine";

    public static final int ROUTING_ENGINE_STRATEGY_ID = 0;

    private class NoAlternateStrategyFound extends Exception {
    }

    private DirectRoutingStrategyApi directStrategy;
    private Map<Integer, RoutingStrategyApi> additionalStrategies;

    private CoreApi core;

    /**
     * Constructor.
     *
     * @param core           reference to the core
     * @param directStrategy reference to the direct routing strategy
     */
    public RoutingEngine(CoreApi core, DirectRoutingStrategyApi directStrategy) {
        this.core = core;
        this.directStrategy = directStrategy;
        this.additionalStrategies = new HashMap<>();
    }

    @Override
    public int getRoutingStrategyId() {
        return ROUTING_ENGINE_STRATEGY_ID;
    }

    @Override
    public String getRoutingStrategyName() {
        return TAG;
    }

    @Override
    public void addRoutingStrategy(RoutingStrategyApi routingStrategy)
            throws RoutingStrategyAlreadyRegistered {
        if (additionalStrategies.containsKey(routingStrategy.getRoutingStrategyId())) {
            throw new RoutingStrategyAlreadyRegistered();
        }
        additionalStrategies.put(routingStrategy.getRoutingStrategyId(), routingStrategy);
    }

    @Override
    public Single<RoutingStrategyResult> route(Bundle bundle) {
        return directStrategy.route(bundle)
                .flatMap(
                        directRoutingResult -> {
                            switch (directRoutingResult) {
                                case Forwarded:
                                    return Single.just(directRoutingResult);
                                case CustodyAccepted:
                                    return Single.error(new IllegalRoutingResult());
                                case CustodyRefused:
                                default:
                                    return directForwardingContraindicated(bundle);
                            }
                        });
    }

    private Single<RoutingStrategyResult> directForwardingContraindicated(Bundle bundle) {
        try {
            return findAlternateStrategy(bundle).route(bundle);
        } catch (NoAlternateStrategyFound nasf) {
            core.getLogger().d(TAG,
                    "falling back on direct strategy");
            return directStrategy.routeLater(bundle);
        }
    }

    private RoutingStrategyApi findAlternateStrategy(Bundle bundle)
            throws NoAlternateStrategyFound {
        if (bundle.hasBlock(RoutingBlock.ROUTING_BLOCK_TYPE)) {
            for (CanonicalBlock block : bundle.getBlocks(RoutingBlock.ROUTING_BLOCK_TYPE)) {
                RoutingBlock rb = (RoutingBlock) block;
                if (additionalStrategies.containsKey(rb.strategyId)) {
                    core.getLogger().i(TAG,
                            "using routing block strategy id: "
                                    + rb.strategyId);
                    return additionalStrategies.get(rb.strategyId);
                } else {
                    core.getLogger().i(TAG,
                            "routing block strategy id unknown: "
                                    + rb.strategyId);
                }
            }
        }
        throw new NoAlternateStrategyFound();
    }


    /*
    private Single<RoutingStrategyResult> forwardingContraindicated(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding contraindicated ("
                + bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code") + "): "
                + bundle.bid.getBidString());

        core.getLogger().v(TAG, "5.4.1-1 " + bundle.bid.getBidString());
        boolean isFailure;
        switch (bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code")) {
            case DepletedStorage:
            case DestinationEIDUnintellegible:
            case BlockUnintelligible:
            case HopLimitExceeded:
            case LifetimeExpired:
                isFailure = true;
                break;
            case NoAdditionalInformation:
            case ForwardedOverUnidirectionalLink:
            case TransmissionCancelled:
            case NoKnownRouteForDestination:
            case NoTimelyContactWithNextNodeOnRoute:
            default:
                bundle.removeTag("reason_code");
                isFailure = false;
                break;
        }

        core.getLogger().v(TAG, "5.4.1-2 " + bundle.bid.getBidString());
        if (isFailure) {
            return Completable.error(new ForwardingFailureException());
        } else {
        core.getLogger().v(TAG, "5.4.1-2 " + bundle.bid.getBidString());
        if (isFailure) {
        bundleForwardingFailed(bundle);
    } else {
        if (!bundle.isTagged("in_storage")) {
            core.getStorage().store(bundle).subscribe(
                    b -> {
                        core.getLogger().v(TAG, "5.4.1-3 " + bundle.bid.getBidString());
                        core.getRoutingEngine().forwardLater(b);
                        endProcessing(bundle);
                    },
                    storageFailure -> {
                        core.getLogger().v(TAG, "5.4.1-2 storage failure: "
                              + storageFailure.getMessage());
                        bundleForwardingFailed(bundle);
                    }
            );
        } else {
            core.getRoutingEngine().forwardLater(bundle);
        }
    }
}
        }
    }
    */
}
