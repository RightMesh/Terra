package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import static io.left.rightmesh.libdtn.common.data.eid.EidIpn.EID_IPN_IANA_VALUE;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidIpn;

/**
 * EidSerializer serializes an {@link Eid}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class EidSerializer {

    /**
     * serializes an {@link Eid}.
     *
     * @param eid to serialize.
     * @return a Cbor-encoded serialized Eid.
     */
    public static CborEncoder encode(Eid eid) {
        if (eid.equals(DtnEid.nullEid())) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.ianaNumber())
                    .cbor_encode_int(0);
        }
        if (eid.ianaNumber() == EID_IPN_IANA_VALUE) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.ianaNumber())
                    .cbor_start_array(2)
                    .cbor_encode_int(((EidIpn) eid).nodeNumber)
                    .cbor_encode_int(((EidIpn) eid).serviceNumber);
        }
        return CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(eid.ianaNumber())
                .cbor_encode_text_string(eid.getSsp());
    }

}
