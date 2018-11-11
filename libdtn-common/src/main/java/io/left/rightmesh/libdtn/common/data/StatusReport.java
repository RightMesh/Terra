package io.left.rightmesh.libdtn.common.data;

import java.util.HashMap;
import java.util.Map;

import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class StatusReport extends AdministrativeRecord {

    public static final int type = 1;

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
    public EID source;
    public Long creationTimestamp;

    public boolean subjectBundleIsFragment = false;
    public Long fragmentOffset;
    public Long appDataLength = null;

    public StatusReport() {
        super(type);
        statusInformation = new HashMap<>();
    }

    public StatusReport(ReasonCode code) {
        super(type);
        statusInformation = new HashMap<>();
        this.code = code;
    }

}
