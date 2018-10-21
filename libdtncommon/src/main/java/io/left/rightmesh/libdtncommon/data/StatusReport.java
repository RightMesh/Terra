package io.left.rightmesh.libdtncommon.data;

import io.left.rightmesh.libdtncommon.data.eid.EID;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class StatusReport extends AdministrativeRecord {

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

    EID reportTo;
    ReasonCode code;

    public StatusReport(EID reportTo, ReasonCode code) {
        super(1);
        this.reportTo = reportTo;
        this.code = code;
    }

}
