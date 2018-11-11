package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AdministrativeRecord;
import io.left.rightmesh.libdtn.common.data.StatusReport;

/**
 * @author Lucien Loiseau on 10/11/18.
 */
public class AdministrativeRecordSerializer {

    public static CborEncoder encode(AdministrativeRecord record) {

        /* encode administrative record header */
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(record.type);

        /* encode administrative record body */
        switch (record.type) {
            case StatusReport.type:
                enc.merge(StatusReportSerializer.encode((StatusReport)record));
                break;
            default:
                enc.cbor_encode_int(0);
        }

        return enc;
    }

}
