package io.left.rightmesh.libdtn.core.processor;

import java.util.NoSuchElementException;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.CanonicalBlock;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.StatusReport;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.ConnectionAgent;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.libdtn.utils.Log;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_AUTO_CONNECT;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_STATUS_REPORTING;
import static io.left.rightmesh.libdtn.core.DTNCore.TAG;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DISCARD_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.PrimaryBlock.BundleV7Flags.DELETION_REPORT;
import static io.left.rightmesh.libdtn.data.PrimaryBlock.BundleV7Flags.DELIVERY_REPORT;
import static io.left.rightmesh.libdtn.data.PrimaryBlock.BundleV7Flags.RECEPTION_REPORT;
import static io.left.rightmesh.libdtn.data.StatusReport.ReasonCode.BlockUnintelligible;
import static io.left.rightmesh.libdtn.data.StatusReport.ReasonCode.LifetimeExpired;
import static io.left.rightmesh.libdtn.data.StatusReport.ReasonCode.NoKnownRouteForDestination;
import static io.left.rightmesh.libdtn.data.StatusReport.ReasonCode.TransmissionCancelled;

/**
 * BundleProcessor is the entry point of all Bundle (either from Application Agent or
 * Convergence Layer) and follows the processing instruction described in the RFC.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public class BundleProcessor {

    private static final String TAG = "BundleProcessor";

    public static boolean reporting() {
        return DTNConfiguration.<Boolean>get(ENABLE_STATUS_REPORTING).value();
    }


    /* 5.2 */
    public static void bundleTransmission(Bundle bundle) {
        /* 5.2 - step 1 */
        if (!bundle.source.equals(EID.NullEID()) && !LocalEIDTable.isLocal(bundle.source)) {
            bundle.source = LocalEIDTable.localEID();
        }
        bundle.tag("dispatch_pending");

        /* 5.2 - step 2 */
        bundleForwarding(bundle);
    }

    /* 5.3 */
    public static void bundleDispatching(Bundle bundle) {
        Log.i(TAG, "dispatching bundle: " + bundle.bid);
        /* 5.3 - step 1 */
        if (LocalEIDTable.isLocal(bundle.destination)) {
            bundleLocalDelivery(bundle);
            return;
        }

        if (DTNConfiguration.<Boolean>get(ENABLE_FORWARDING).value()) {
            /* 5.3 - step 2 */
            bundleForwarding(bundle);
        } else {
            bundle.tag("reason_code", NoKnownRouteForDestination);
            bundleDeletion(bundle);
        }
    }

    /* 5.4 */
    public static void bundleForwarding(Bundle bundle) {
        /* 5.4 - step 1 */
        bundle.removeTag("dispatch_pending");
        bundle.tag("forward_pending");

        /* 5.4 - step 2 */
        try {
            RoutingEngine.findCLA(bundle.destination)
                    .flatMapMaybe(claChannel ->
                            claChannel.sendBundle(bundle)
                            .lastElement()
                            .onErrorComplete())
                    .blockingFirst();

            /* 5.4 - step 5 */
            bundleForwardingSuccessful(bundle);
        } catch (NoSuchElementException nse) {
            /* 5.4 - step 3 */
            bundle.tag("reason_code", NoKnownRouteForDestination);
            bundleForwardingContraindicated(bundle);
        }
    }

    /* 5.4 - step 5 */
    public static void bundleForwardingSuccessful(Bundle bundle) {
        bundle.removeTag("forward_pending");
        bundleDiscarding(bundle);
    }


    /* 5.4.1 */
    public static void bundleForwardingContraindicated(Bundle bundle) {
        Log.i(TAG, "forwarding contraindicated ("+bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code")+ "): " + bundle.bid);

        /* 5.4.1 - step 1 */
        boolean is_failure;
        switch (bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code")) {
            case DepletedStorage:
            case DestinationEIDUnintellegible:
            case BlockUnintelligible:
            case HopLimitExceeded:
            case LifetimeExpired:
                is_failure = true;
                break;
            case NoAdditionalInformation:
            case ForwardedOverUnidirectionalLink:
            case TransmissionCancelled:
            case NoKnownRouteForDestination:
            case NoTimelyContactWithNextNodeOnRoute:
            default:
                bundle.removeTag("reason_code");
                is_failure = false;
                break;
        }

        /* 5.4.1 - step 2 */
        if (is_failure) {
            bundleForwardingFailed(bundle);
        } else {
            if (!bundle.isTagged("in_storage")) {
                Storage.store(bundle).subscribe(
                        b -> {
                            /* in storage, defer forwarding */
                            RoutingEngine.forwardLater(b);
                        },
                        storageFailure -> {
                            /* storage failed, abandon forwarding */
                            bundleForwardingFailed(bundle);
                        }
                );
            }
        }
    }

    /* 5.4.2 */
    public static void bundleForwardingFailed(Bundle bundle) {
        /* 5.4.2 - step 1 */
        // atm we never send the bundle back to the source

        /* 5.4.2 - step 2 */
        if (LocalEIDTable.isLocal(bundle.destination)) {
            bundle.removeTag("forward_pending");
            bundleDiscarding(bundle);
        } else {
            bundleDeletion(bundle);
        }

    }

    /* 5.5 */
    public static void bundleExpired(Bundle bundle) {
        bundle.tag("reason_code", LifetimeExpired);
        bundleDeletion(bundle);
    }

    /* 5.6 */
    public static void bundleReception(Bundle bundle) {
        /* 5.6 - step 1 */
        bundle.tag("dispatch_pending");

        /* 5.6 - step 2 */
        if (bundle.getV7Flag(RECEPTION_REPORT) && reporting()) {
            // todo generate reception status report
        }

        /* 5.6 - step 3 */
        try {
            for (CanonicalBlock block : bundle.getBlocks()) {
                try {
                    block.onReceptionProcessing(bundle);
                } catch (ProcessorNotFoundException pe) {
                    if (block.getV7Flag(TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED) && reporting()) {
                        // todo create a status report
                    }
                    if (block.getV7Flag(DELETE_BUNDLE_IF_NOT_PROCESSED)) {
                        bundle.tag("reason_code", BlockUnintelligible);
                        throw new ProcessingException();
                    }
                    if (block.getV7Flag(DISCARD_IF_NOT_PROCESSED)) {
                        bundle.delBlock(block);
                    }
                }
            }
        } catch (ProcessingException e) {
            bundleDeletion(bundle);
            return;
        }

        /* 5.6 - step 4 */
        bundleDispatching(bundle);
    }

    /* 5.7 */
    public static void bundleLocalDelivery(Bundle bundle) {
        bundle.tag("delivery_pending");
        /* 5.7 - step 1 */
        // atm we don't support fragmentation

        /* 5.7 - step 2 */
        EID localMatch = LocalEIDTable.matchLocal(bundle.destination);
        if (localMatch != null) {
            String sink = bundle.destination.eid.replaceFirst(localMatch.eid, "");
            AARegistrar.deliver(sink, bundle).subscribe(
                    () -> bundleLocalDeliverySuccessful(bundle),
                    deliveryFailure -> bundleLocalDeliveryFailure(sink, bundle));
        } else {
            // it should never happen because we already checked that the bundle was local.
            // but if the configuration changed right when the thread was jumping here it
            // may happen. In such unlikely event, we simply go back to bundleDispatching.
            bundleDispatching(bundle);
        }
    }

    /* 5.7 - step 3 */
    public static void bundleLocalDeliverySuccessful(Bundle bundle) {
        bundle.removeTag("delivery_pending");
        if (bundle.getV7Flag(DELIVERY_REPORT) && reporting()) {
            // todo generate status report
        }
        bundleDeletion(bundle);
    }

    /* 5.7 - step 2 - delivery failure */
    public static void bundleLocalDeliveryFailure(String sink, Bundle bundle) {
        if (!bundle.isTagged("in_storage")) {
            Storage.store(bundle).subscribe(
                    b -> {
                        /* register for event and deliver later */
                        AARegistrar.deliverLater(sink, bundle);
                    },
                    storageFailure -> {
                        /* abandon delivery */
                        bundleDeletion(bundle);
                    }
            );
        }
    }

    /* 5.8 */
    public static void bundleFragmentation(Bundle bundle) {
        // not supported atm
    }

    /* 5.10 */
    public static void bundleDeletion(Bundle bundle) {
        /* 5.10 - step 1 */
        if (bundle.getV7Flag(DELETION_REPORT) && reporting()) {
            // todo generate deletion report
        }

        /* 5.10 - step 2 */
        bundle.removeTag("dispatch_pending");
        bundle.removeTag("forward_pending");
        bundle.removeTag("delivery_pending");
        bundleDiscarding(bundle);
    }

    /* 5.11 */
    public static void bundleDiscarding(Bundle bundle) {
        Storage.remove(bundle.bid).subscribe(
                () -> {
                },
                e -> {
                    bundle.getPayloadBlock().data.getWritableBLOB().clear();
                }
        );
    }

}
