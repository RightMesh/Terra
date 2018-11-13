package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.IPN;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class EIDSerializer {

    public static CborEncoder encode(EID eid) {
        if (eid.equals(DTN.NullEID())) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_encode_int(0);
        }
        if (eid.IANA() == EID.EID_IPN_IANA_VALUE) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_start_array(2)
                    .cbor_encode_int(((IPN) eid).node_number)
                    .cbor_encode_int(((IPN) eid).service_number);
        }
        return CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(eid.IANA())
                .cbor_encode_text_string(eid.getSsp());
    }

}
