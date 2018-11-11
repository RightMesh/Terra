package io.left.rightmesh.libdtn.core.processor;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.AdministrativeRecordSerializer;
import io.left.rightmesh.libdtn.common.data.eid.API;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.ProcessingException;
import io.left.rightmesh.libdtn.common.data.ProcessorNotFoundException;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.StatusReport;
import io.left.rightmesh.libdtn.core.api.BundleProcessorAPI;
import io.left.rightmesh.libdtn.core.utils.ClockUtil;

import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.NoAdditionalInformation;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeDeletedBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeDeliveredBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeForwardedBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeReceivedBundle;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.DISCARD_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.DELETION_REPORT;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.DELIVERY_REPORT;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.RECEPTION_REPORT;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.BlockUnintelligible;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.LifetimeExpired;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.NoKnownRouteForDestination;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_STATUS_REPORTING;

/**
 * BundleProcessor is the entry point of all Bundle (either from Application Agent or
 * Convergence Layer) and follows the processing instruction described in the RFC.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public class BundleProcessor implements BundleProcessorAPI {

    private static final String TAG = "BundleProcessor";

    private DTNCore core;

    public BundleProcessor(DTNCore core) {
        this.core = core;
    }

    public boolean reporting() {
        return core.getConf().<Boolean>get(ENABLE_STATUS_REPORTING).value();
    }

    /* 5.2 */
    public void bundleTransmission(Bundle bundle) {
        /* 5.2 - step 1 */
        core.getLogger().v(TAG, "5.2-1 " + bundle.bid.getBIDString());
        if (!bundle.source.equals(DTN.NullEID()) && !core.getLocalEID().isLocal(bundle.source)) {
            bundle.source = core.getLocalEID().localEID();
        }
        bundle.tag("dispatch_pending");

        /* 5.2 - step 2 */
        core.getLogger().v(TAG, "5.2-2 " + bundle.bid.getBIDString());
        bundleDispatching(bundle);
        // the RFC states to jump to bundleForwarding but I consider that a transmission may also
        // be for local delivery
    }

    /* 5.3 */
    public void bundleDispatching(Bundle bundle) {
        core.getLogger().i(TAG, "dispatching bundle: " + bundle.bid.getBIDString() + " to EID: " + bundle.destination.getEIDString());

        /* 5.3 - step 1 */
        core.getLogger().v(TAG, "5.3-1: " + bundle.bid.getBIDString());
        if (core.getLocalEID().isLocal(bundle.destination)) {
            bundleLocalDelivery(bundle);
            return;
        }

        if (core.getConf().<Boolean>get(ENABLE_FORWARDING).value()) {
            /* 5.3 - step 2 */
            core.getLogger().v(TAG, "5.3-2: " + bundle.bid.getBIDString());
            bundleForwarding(bundle);
        } else {
            bundle.tag("reason_code", NoKnownRouteForDestination);
            bundleDeletion(bundle);
        }
    }

    /* 5.4 */
    public void bundleForwarding(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding bundle: " + bundle.bid.getBIDString());

        /* 5.4 - step 1 */
        core.getLogger().v(TAG, "5.4-1 " + bundle.bid.getBIDString());
        bundle.removeTag("dispatch_pending");
        bundle.tag("forward_pending");

        /* 5.4 - step 2 */
        core.getLogger().v(TAG, "5.4-2 " + bundle.bid.getBIDString());
        core.getRoutingEngine().findOpenedChannelTowards(bundle.destination)
                .concatMapMaybe(claChannel ->
                        claChannel.sendBundle(bundle)  /* 5.4 - step 4 */
                                .lastElement()
                                .doOnError(System.out::println)
                                .onErrorComplete())
                .firstElement()
                .subscribe(
                        (i) -> {
                            /* 5.4 - step 5 */
                            core.getLogger().v(TAG, "5.4-5 " + bundle.bid.getBIDString());
                            bundleForwardingSuccessful(bundle);
                        },
                        e -> {
                            /* 5.4 - step 3 */
                            core.getLogger().v(TAG, "5.4-3 " + bundle.bid.getBIDString());
                            bundle.tag("reason_code", NoKnownRouteForDestination);
                            bundleForwardingContraindicated(bundle);
                        },
                        () -> {
                            /* 5.4 - step 3 */
                            core.getLogger().v(TAG, "5.4-3 " + bundle.bid.getBIDString());
                            bundle.tag("reason_code", NoKnownRouteForDestination);
                            bundleForwardingContraindicated(bundle);
                        }
                );
    }

    /* 5.4 - step 5 */
    public void bundleForwardingSuccessful(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding successful: " + bundle.bid.getBIDString());
        bundle.removeTag("forward_pending");
        createStatusReport(ReportingNodeForwardedBundle, bundle, NoAdditionalInformation);
        bundleDiscarding(bundle);
    }

    /* 5.4.1 */
    public void bundleForwardingContraindicated(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding contraindicated (" + bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code") + "): " + bundle.bid.getBIDString());

        /* 5.4.1 - step 1 */
        core.getLogger().v(TAG, "5.4.1-1 " + bundle.bid.getBIDString());
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
        core.getLogger().v(TAG, "5.4.1-2 " + bundle.bid.getBIDString());
        if (is_failure) {
            bundleForwardingFailed(bundle);
        } else {
            if (!bundle.isTagged("in_storage")) {
                core.getStorage().store(bundle).subscribe(
                        b -> {
                            /* 5.4.1 - step 3 - in Storage, defer forwarding */
                            core.getLogger().v(TAG, "5.4.1-3 " + bundle.bid.getBIDString());
                            core.getRoutingEngine().forwardLater(b);
                            endProcessing(bundle);
                        },
                        storageFailure -> {
                            /* 5.4.1 - step 2 -  Storage failed, abandon forwarding */
                            bundleForwardingFailed(bundle);
                        }
                );
            } else {
                core.getRoutingEngine().forwardLater(bundle);
            }
        }
    }

    /* 5.4.2 */
    public void bundleForwardingFailed(Bundle bundle) {
        /* 5.4.2 - step 1 */
        core.getLogger().v(TAG, "5.4.2-1 " + bundle.bid.getBIDString());
        // atm we never send the bundle back to the source

        /* 5.4.2 - step 2 */
        core.getLogger().v(TAG, "5.4.2-2 " + bundle.bid.getBIDString());
        if (core.getLocalEID().isLocal(bundle.destination)) {
            bundle.removeTag("forward_pending");
            bundleDiscarding(bundle);
        } else {
            bundleDeletion(bundle);
        }

    }

    /* 5.5 */
    public void bundleExpired(Bundle bundle) {
        core.getLogger().v(TAG, "5.5 " + bundle.bid.getBIDString());
        bundle.tag("reason_code", LifetimeExpired);
        bundleDeletion(bundle);
    }

    /* 5.6 */
    public void bundleReception(Bundle bundle) {
        /* 5.6 - step 1 */
        core.getLogger().v(TAG, "5.6-1 " + bundle.bid.getBIDString());
        bundle.tag("dispatch_pending");

        /* 5.6 - step 2 */
        core.getLogger().v(TAG, "5.6-2 " + bundle.bid.getBIDString());
        if (bundle.getV7Flag(RECEPTION_REPORT) && reporting()) {
            createStatusReport(ReportingNodeReceivedBundle, bundle, NoAdditionalInformation);
        }

        /* 5.6 - step 3 */
        core.getLogger().v(TAG, "5.6-3 " + bundle.bid.getBIDString());
        try {
            for (CanonicalBlock block : bundle.getBlocks()) {
                try {
                    block.onReceptionProcessing(bundle);
                } catch (ProcessorNotFoundException pe) {
                    if (block.getV7Flag(TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED) && reporting()) {
                        createStatusReport(ReportingNodeReceivedBundle, bundle, BlockUnintelligible);
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
        core.getLogger().v(TAG, "5.6-4 " + bundle.bid.getBIDString());
        bundleDispatching(bundle);
    }

    /* 5.7 */
    public void bundleLocalDelivery(Bundle bundle) {
        bundle.tag("delivery_pending");
        /* 5.7 - step 1 */
        core.getLogger().v(TAG, "5.7-1 " + bundle.bid.getBIDString());
        // atm we don't support fragmentation

        /* 5.7 - step 2 */
        core.getLogger().v(TAG, "5.7-2 " + bundle.bid.getBIDString());
        EID localMatch = core.getLocalEID().matchLocal(bundle.destination);
        if (localMatch != null) {
            String sink = bundle.destination.getEIDString().replaceFirst(localMatch.getEIDString(), "");
            core.getDelivery().deliver(sink, bundle).subscribe(
                    () -> bundleLocalDeliverySuccessful(bundle),
                    deliveryFailure -> bundleLocalDeliveryFailure(sink, bundle));
        } else {
            // it should never happen because we already checked that the bundle was local (5.3).
            // but if the configuration changed right when the thread was jumping here it
            // may happen. In such unlikely event, we simply go back to bundleDispatching.
            bundleDispatching(bundle);
        }
    }

    /* 5.7 - step 3 */
    public void bundleLocalDeliverySuccessful(Bundle bundle) {
        core.getLogger().i(TAG, "bundle successfully delivered: " + bundle.bid.getBIDString());
        bundle.removeTag("delivery_pending");
        if (bundle.getV7Flag(DELIVERY_REPORT) && reporting()) {
            createStatusReport(ReportingNodeDeliveredBundle, bundle, NoAdditionalInformation);
        }
        bundleDeletion(bundle);
    }

    /* 5.7 - step 2 - delivery failure */
    public void bundleLocalDeliveryFailure(String sink, Bundle bundle) {
        core.getLogger().i(TAG, "bundle could not be delivered sink=" + sink + " bundleID=" + bundle.bid.getBIDString());
        if (!bundle.isTagged("in_storage")) {
            core.getStorage().store(bundle).subscribe(
                    b -> {
                        /* register for event and deliver later */
                        core.getDelivery().deliverLater(sink, bundle);
                        endProcessing(bundle);
                    },
                    storageFailure -> {
                        /* abandon delivery */
                        core.getLogger().w(TAG, "storage failure: " + storageFailure.getMessage());
                        bundleDeletion(bundle);
                    }
            );
        } else {
            /* register for event and deliver later */
            core.getDelivery().deliverLater(sink, bundle);
        }
    }

    /* 5.8 */
    public void bundleFragmentation(Bundle bundle) {
        // not supported atm
        core.getLogger().v(TAG, "5.8 " + bundle.bid.getBIDString());
    }

    /* 5.10 */
    public void bundleDeletion(Bundle bundle) {
        core.getLogger().i(TAG, "deleting bundle (" + bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code") + "): " + bundle.bid.getBIDString());

        /* 5.10 - step 1 */
        core.getLogger().v(TAG, "5.10-2 " + bundle.bid.getBIDString());
        if (bundle.getV7Flag(DELETION_REPORT) && reporting()) {
            createStatusReport(ReportingNodeDeletedBundle, bundle, NoAdditionalInformation);
        }

        /* 5.10 - step 2 */
        core.getLogger().v(TAG, "5.10-2 " + bundle.bid.getBIDString());
        bundle.removeTag("dispatch_pending");
        bundle.removeTag("forward_pending");
        bundle.removeTag("delivery_pending");
        bundleDiscarding(bundle);
    }

    /* 5.11 */
    public void bundleDiscarding(Bundle bundle) {
        core.getLogger().i(TAG, "discarding bundle: " + bundle.bid.getBIDString());
        core.getStorage().remove(bundle.bid).subscribe(
                bundle::clearBundle,
                e -> {
                    bundle.clearBundle();
                }
        );
        endProcessing(bundle);
    }

    /* not in RFC - end processing for this bundle, send all status report if any */
    public void endProcessing(Bundle bundle) {
        if (bundle.isTagged("status-reports")) {
            List<Bundle> reports = bundle.getTagAttachment("status-reports");
            for (Bundle report : reports) {
                core.getLogger().i(TAG, "sending status report to: " + bundle.destination.getEIDString());
                bundleTransmission(report);
            }
        }
    }

    /* create status report */
    public void createStatusReport(StatusReport.StatusAssertion assertion, Bundle bundle, StatusReport.ReasonCode reasonCode) {
        if (bundle.reportto.equals(DTN.NullEID())) {
            return;
        }

        StatusReport statusReport = new StatusReport(reasonCode);
        statusReport.source = bundle.source;
        statusReport.creationTimestamp = bundle.creationTimestamp;
        if (assertion.equals(ReportingNodeDeletedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.DELETION_REPORT)) {
            statusReport.statusInformation.put(ReportingNodeDeletedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeForwardedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.FORWARD_REPORT)) {
            statusReport.statusInformation.put(ReportingNodeForwardedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeReceivedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.RECEPTION_REPORT)) {
            statusReport.statusInformation.put(ReportingNodeReceivedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeDeliveredBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.DELIVERY_REPORT)) {
            statusReport.statusInformation.put(ReportingNodeDeliveredBundle, ClockUtil.getCurrentTime());
        } else {
            return;
        }

        /* create the bundle that will carry this status report back to the reporting node */
        Bundle report = new Bundle(bundle.reportto);

        /* get size of status report for the payload */
        CborEncoder enc = AdministrativeRecordSerializer.encode(statusReport);
        long size = enc.observe()
                .map(ByteBuffer::remaining)
                .reduce(0, (a, b) -> a + b)
                .blockingGet();

        /* serialize the status report into the bundle payload */
        UntrackedByteBufferBLOB blobReport = new UntrackedByteBufferBLOB((int) size);
        final WritableBLOB wblob = blobReport.getWritableBLOB();
        enc.observe()
                .map(wblob::write)
                .doOnComplete(wblob::close)
                .subscribe();
        report.addBlock(new PayloadBlock(blobReport));

        /* attach the status report to the bundle (it will be send during endProcessing) */
        List<Bundle> reports = bundle.getTagAttachment("status-reports");
        if (reports == null) {
            reports = new LinkedList<>();
            bundle.tag("status-reports", reports);
        }
        reports.add(report);
    }

}
