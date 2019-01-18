package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AdministrativeRecord;
import io.left.rightmesh.libdtn.common.data.StatusReport;

/**
 * AdministrativeRecordSerializer serializes an AdministrativeRecord.
 *
 * @author Lucien Loiseau on 10/11/18.
 */
public class AdministrativeRecordSerializer {

    /**
     * serializes an AdministrativeRecord.
     *
     * @param record to serialize
     * @return the Cbor-Encoded record.
     */
    public static CborEncoder encode(AdministrativeRecord record) {

        /* encode administrative record header */
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(record.type);

        /* encode administrative record body */
        switch (record.type) {
            case StatusReport.STATUS_REPORT_ADM_TYPE:
                enc.merge(StatusReportSerializer.encode((StatusReport) record));
                break;
            default:
                enc.cbor_encode_int(0);
        }

        return enc;
    }

}
