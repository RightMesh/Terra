package io.left.rightmesh.libdtn.core.processor;

import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.DISCARD_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.DELETION_REPORT;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.DELIVERY_REPORT;
import static io.left.rightmesh.libdtn.common.data.PrimaryBlock.BundleV7Flags.RECEPTION_REPORT;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.BlockUnintelligible;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.LifetimeExpired;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.NoAdditionalInformation;
import static io.left.rightmesh.libdtn.common.data.StatusReport.ReasonCode.NoKnownRouteForDestination;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeDeletedBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeDeliveredBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeForwardedBundle;
import static io.left.rightmesh.libdtn.common.data.StatusReport.StatusAssertion.ReportingNodeReceivedBundle;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.ENABLE_STATUS_REPORTING;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.StatusReport;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBlob;
import io.left.rightmesh.libdtn.common.data.blob.WritableBlob;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.data.bundlev7.serializer.AdministrativeRecordSerializer;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.BundleProtocolApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.utils.ClockUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * BundleProtocol is the entry point of all Bundle (either from Application Agent or
 * Convergence Layer) and follows the processing instruction described in the RFC.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public class BundleProtocol implements BundleProtocolApi {

    private static final String TAG = "BundleProtocol";

    private CoreApi core;

    public BundleProtocol(CoreApi core) {
        this.core = core;
    }

    private boolean reporting() {
        return core.getConf().<Boolean>get(ENABLE_STATUS_REPORTING).value();
    }

    /* 5.2 */
    @Override
    public void bundleTransmission(Bundle bundle) {
        /* 5.2 - step 1 */
        core.getLogger().v(TAG, "5.2-1 " + bundle.bid.getBidString());
        if (!bundle.getSource().equals(DtnEid.nullEid())
                && !core.getLocalEid().isLocal(bundle.getSource())) {
            bundle.setSource(core.getLocalEid().localEid());
        }
        bundle.tag("dispatch_pending");

        /* 5.2 - step 2 */
        core.getLogger().v(TAG, "5.2-2 " + bundle.bid.getBidString());
        bundleForwarding(bundle);
    }

    /* 5.3 */
    @Override
    public void bundleDispatching(Bundle bundle) {
        core.getLogger().i(TAG, "dispatching bundle: " + bundle.bid.getBidString()
                + " to Eid: " + bundle.getDestination().getEidString());

        /* 5.3 - step 1 */
        core.getLogger().v(TAG, "5.3-1: " + bundle.bid.getBidString());
        if (core.getLocalEid().isLocal(bundle.getDestination())) {
            bundleLocalDelivery(bundle);
            return;
        }

        if (core.getConf().<Boolean>get(ENABLE_FORWARDING).value()) {
            /* 5.3 - step 2 */
            core.getLogger().v(TAG, "5.3-2: " + bundle.bid.getBidString());
            bundleForwarding(bundle);
        } else {
            bundle.tag("reason_code", NoKnownRouteForDestination);
            bundleDeletion(bundle);
        }
    }

    /* 5.4 */
    private void bundleForwarding(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding bundle: " + bundle.bid.getBidString());

        /* 5.4 - step 1 */
        bundle.removeTag("dispatch_pending");
        bundle.tag("forward_pending");

        /* 5.4 - step 2 */
        core.getRoutingEngine().route(bundle).subscribe(
                (routingResult) -> {
                    switch (routingResult) {
                        case Forwarded:
                            bundleForwardingSuccessful(bundle);
                            break;
                        case CustodyRefused:
                            bundleForwardingFailed(bundle);
                            break;
                        case CustodyAccepted:
                            endProcessing(bundle);
                            break;
                        default:
                    }
                },
                routingError -> bundleForwardingFailed(bundle));
    }

    /* 5.4 - step 5 */
    @Override
    public void bundleForwardingSuccessful(Bundle bundle) {
        core.getLogger().d(TAG, "forwarding successful: " + bundle.bid.getBidString());
        bundle.removeTag("forward_pending");
        createStatusReport(ReportingNodeForwardedBundle, bundle, NoAdditionalInformation);
        bundleDiscarding(bundle);
    }

    /* 5.4.2 */
    private void bundleForwardingFailed(Bundle bundle) {
        /* 5.4.2 - step 1 */
        core.getLogger().v(TAG, "5.4.2-1 " + bundle.bid.getBidString());
        // atm we never send the bundle back to the source

        /* 5.4.2 - step 2 */
        core.getLogger().v(TAG, "5.4.2-2 " + bundle.bid.getBidString());
        if (core.getLocalEid().isLocal(bundle.getDestination())) {
            bundle.removeTag("forward_pending");
            bundleDiscarding(bundle);
        } else {
            bundleDeletion(bundle);
        }

    }

    /* 5.5 */
    @Override
    public void bundleExpired(Bundle bundle) {
        core.getLogger().v(TAG, "5.5 " + bundle.bid.getBidString());
        bundle.tag("reason_code", LifetimeExpired);
        bundleDeletion(bundle);
    }

    /* 5.6 */
    @Override
    public void bundleReception(Bundle bundle) {
        /* 5.6 - step 1 */
        core.getLogger().v(TAG, "5.6-1 " + bundle.bid.getBidString());
        bundle.tag("dispatch_pending");

        /* 5.6 - step 2 */
        core.getLogger().v(TAG, "5.6-2 " + bundle.bid.getBidString());
        if (bundle.getV7Flag(RECEPTION_REPORT) && reporting()) {
            createStatusReport(ReportingNodeReceivedBundle, bundle, NoAdditionalInformation);
        }

        /* 5.6 - step 3 */
        core.getLogger().v(TAG, "5.6-3 " + bundle.bid.getBidString());
        try {
            for (CanonicalBlock block : bundle.getBlocks()) {
                try {
                    core.getExtensionManager().getBlockProcessorFactory().create(block.type)
                            .onReceptionProcessing(block, bundle, core.getLogger());
                } catch (BlockProcessorFactory.ProcessorNotFoundException pe) {
                    if (block.getV7Flag(TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED) && reporting()) {
                        createStatusReport(
                                ReportingNodeReceivedBundle,
                                bundle,
                                BlockUnintelligible);
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
        core.getLogger().v(TAG, "5.6-4 " + bundle.bid.getBidString());
        bundleDispatching(bundle);
    }

    /* 5.7 */
    private void bundleLocalDelivery(Bundle bundle) {
        bundle.tag("delivery_pending");
        /* 5.7 - step 1 */
        core.getLogger().v(TAG, "5.7-1 " + bundle.bid.getBidString());
        // atm we don't support fragmentation

        /* 5.7 - step 2 */
        core.getLogger().v(TAG, "5.7-2 " + bundle.bid.getBidString());
        Eid localMatch = core.getLocalEid().matchLocal(bundle.getDestination());
        if (localMatch != null) {
            String sink = bundle.getDestination().getEidString()
                    .replaceFirst(localMatch.getEidString(), "");
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
    @Override
    public void bundleLocalDeliverySuccessful(Bundle bundle) {
        core.getLogger().i(TAG, "bundle successfully delivered: " + bundle.bid.getBidString());
        bundle.removeTag("delivery_pending");
        if (bundle.getV7Flag(DELIVERY_REPORT) && reporting()) {
            createStatusReport(ReportingNodeDeliveredBundle, bundle, NoAdditionalInformation);
        }
        bundleDeletion(bundle);
    }

    /* 5.7 - step 2 - delivery failure */
    @Override
    public void bundleLocalDeliveryFailure(String sink, Bundle bundle) {
        core.getLogger().i(TAG, "bundle could not be delivered sink=" + sink + " bundleID="
                + bundle.bid.getBidString());
        if (!bundle.isTagged("in_storage")) {
            core.getStorage().store(bundle).subscribe(
                    b -> {
                        /* register for event and deliver later */
                        core.getDelivery().deliverLater(sink, bundle);
                        endProcessing(bundle);
                    },
                    storageFailure -> {
                        /* abandon delivery */
                        core.getLogger().w(TAG, "storage failure: "
                                + storageFailure.getMessage());
                        bundleDeletion(bundle);
                    }
            );
        } else {
            /* register for event and deliver later */
            core.getDelivery().deliverLater(sink, bundle);
        }
    }

    /* 5.8 */
    private void bundleFragmentation(Bundle bundle) {
        // not supported atm
        core.getLogger().v(TAG, "5.8 " + bundle.bid.getBidString());
    }

    /* 5.10 */
    private void bundleDeletion(Bundle bundle) {
        core.getLogger().i(TAG, "deleting bundle ("
                + bundle.<StatusReport.ReasonCode>getTagAttachment("reason_code") + "): "
                + bundle.bid.getBidString());

        /* 5.10 - step 1 */
        core.getLogger().v(TAG, "5.10-2 " + bundle.bid.getBidString());
        if (bundle.getV7Flag(DELETION_REPORT) && reporting()) {
            createStatusReport(ReportingNodeDeletedBundle, bundle, NoAdditionalInformation);
        }

        /* 5.10 - step 2 */
        core.getLogger().v(TAG, "5.10-2 " + bundle.bid.getBidString());
        bundle.removeTag("dispatch_pending");
        bundle.removeTag("forward_pending");
        bundle.removeTag("delivery_pending");
        bundleDiscarding(bundle);
    }

    /* 5.11 */
    private void bundleDiscarding(Bundle bundle) {
        core.getLogger().i(TAG, "discarding bundle: " + bundle.bid.getBidString());
        core.getStorage().remove(bundle.bid).subscribe(
                bundle::clearBundle,
                e -> {
                    bundle.clearBundle();
                }
        );
        endProcessing(bundle);
    }

    /* not in RFC - end processing for this bundle, send all status report if any */
    private void endProcessing(Bundle bundle) {
        if (bundle.isTagged("status-reports")) {
            List<Bundle> reports = bundle.getTagAttachment("status-reports");
            for (Bundle report : reports) {
                core.getLogger().i(TAG, "sending status report to: "
                        + report.getDestination().getEidString());
                report.setSource(core.getLocalEid().localEid());
                bundleDispatching(report);
            }
        }
    }

    /* create status report */
    private void createStatusReport(StatusReport.StatusAssertion assertion,
                                    Bundle bundle,
                                    StatusReport.ReasonCode reasonCode) {
        if (bundle.getReportto().equals(DtnEid.nullEid())) {
            return;
        }

        StatusReport statusReport = new StatusReport(reasonCode);
        statusReport.source = bundle.getSource();
        statusReport.creationTimestamp = bundle.getCreationTimestamp();
        if (assertion.equals(ReportingNodeDeletedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.DELETION_REPORT)) {
            statusReport.statusInformation
                    .put(ReportingNodeDeletedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeForwardedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.FORWARD_REPORT)) {
            statusReport.statusInformation
                    .put(ReportingNodeForwardedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeReceivedBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.RECEPTION_REPORT)) {
            statusReport.statusInformation
                    .put(ReportingNodeReceivedBundle, ClockUtil.getCurrentTime());
        } else if (assertion.equals(ReportingNodeDeliveredBundle)
                && bundle.getV7Flag(PrimaryBlock.BundleV7Flags.DELIVERY_REPORT)) {
            statusReport.statusInformation
                    .put(ReportingNodeDeliveredBundle, ClockUtil.getCurrentTime());
        } else {
            return;
        }

        /* create the bundle that will carry this status report back to the reporting node */
        Bundle report = new Bundle(bundle.getReportto());

        /* get size of status report for the payload */
        CborEncoder enc = AdministrativeRecordSerializer.encode(statusReport);
        long size = enc.observe()
                .map(ByteBuffer::remaining)
                .reduce(0, (a, b) -> a + b)
                .blockingGet();

        /* serialize the status report into the bundle payload */
        UntrackedByteBufferBlob blobReport = new UntrackedByteBufferBlob((int) size);
        final WritableBlob wblob = blobReport.getWritableBlob();
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
