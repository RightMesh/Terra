package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.util.HashMap;
import java.util.Map;

/**
 * StatusReport is a special class of {@link AdministrativeRecord} whose purpose is to provide
 * status information report for a specific bundle such as delivery, forwarding, deletion or
 * reception notification.
 *
 * @author Lucien Loiseau on 28/09/18.
 */
public class StatusReport extends AdministrativeRecord {

    public static final int STATUS_REPORT_ADM_TYPE = 1;

    public enum StatusAssertion {
        ReportingNodeReceivedBundle,
        ReportingNodeForwardedBundle,
        ReportingNodeDeliveredBundle,
        ReportingNodeDeletedBundle
    }

    public enum ReasonCode {
        NoAdditionalInformation,
        LifetimeExpired,
        ForwardedOverUnidirectionalLink,
        TransmissionCancelled,
        DepletedStorage,
        DestinationEIDUnintellegible,
        NoKnownRouteForDestination,
        NoTimelyContactWithNextNodeOnRoute,
        BlockUnintelligible,
        HopLimitExceeded,
        Other
    }

    public Map<StatusAssertion, Long> statusInformation;
    public ReasonCode code;
    public Eid source;
    public Long creationTimestamp;

    public boolean subjectBundleIsFragment = false;
    public Long fragmentOffset;
    public Long appDataLength = null;

    public StatusReport() {
        super(STATUS_REPORT_ADM_TYPE);
        statusInformation = new HashMap<>();
    }

    /**
     * Constructor.
     *
     * @param code reason code
     */
    public StatusReport(ReasonCode code) {
        super(STATUS_REPORT_ADM_TYPE);
        statusInformation = new HashMap<>();
        this.code = code;
    }

}
