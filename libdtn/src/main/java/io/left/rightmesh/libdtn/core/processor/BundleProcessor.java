package io.left.rightmesh.libdtn.core.processor;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.CanonicalBlock;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.StatusReport;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.storage.SimpleStorage;
import io.left.rightmesh.libdtn.storage.VolatileStorage;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.ENABLE_STATUS_REPORTING;
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
 * @author Lucien Loiseau on 28/09/18.
 */
public class BundleProcessor {

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
        CLAChannel claChannel = RoutingEngine.findCLA(bundle);
        if (claChannel == null) {
            /* 5.4 - step 3 */
            bundle.tag("reason_code", NoKnownRouteForDestination);
            bundleForwardingContraindicated(bundle);
            return;
        }

        /* 5.4 - step 4 */
        claChannel.sendBundle(bundle).subscribe(
                i -> { /* ignore transmission progress */ },
                e -> {
                    // if transmission has failed it is as if no CLA were found
                    bundle.tag("reason_code", TransmissionCancelled);
                    bundleForwardingContraindicated(bundle);
                },
                () -> {
                    /* 5.4 - step 5 */
                    bundle.removeTag("forward_pending");
                    bundleDiscarding(bundle);
                }
        );
    }

    /* 5.4.1 */
    public static void bundleForwardingContraindicated(Bundle bundle) {
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
            RoutingEngine.forwardLater(bundle);
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
        /* 5.7 - step 1 */
        // atm we don't support fragmentation

        /* 5.7 - step 2 */
        EID localMatch = LocalEIDTable.matchLocal(bundle.destination);
        if (localMatch != null) {
            String sink = bundle.destination.eid.replaceFirst(localMatch.eid, "");
            RegistrationTable.deliver(sink, bundle).subscribe(
                    () -> {
                        /* 5.7 - step 3 */
                        if (bundle.getV7Flag(DELIVERY_REPORT) && reporting()) {
                            // todo generate status report
                        }
                    },
                    e -> {
                        /* 5.7 - step 2 - delivery failure - defer */
                        RegistrationTable.deliverLater(sink, bundle);
                    });
        } else {
            // it should never happen because we already checked that the bundle was local.
            // but if the configuration changed right when the thread was jumping here it
            // may happen. In such unlikely event, we simply go back to bundleDispatching.
            bundleDispatching(bundle);
        }
    }

    /* 5.8 */
    public static void bundleFragmentation(Bundle bundle) {
        // not supported atm
    }

    /* 5.10 */
    public static void bundleDeletion(Bundle bundle) {
        /* 5.10 - step 1 */
        if(bundle.getV7Flag(DELETION_REPORT) && reporting()) {
            // todo generate deletion report
        }

        /* 5.10 - step 2 */
        bundle.removeTag("dispatch_pending");
        bundle.removeTag("forward_pending");
        bundleDiscarding(bundle);
    }

    /* 5.11 */
    public static void bundleDiscarding(Bundle bundle) {
        VolatileStorage.getInstance().remove(bundle.bid).subscribe();
        SimpleStorage.getInstance().remove(bundle.bid).subscribe();
    }

}
