package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityResult;

import java.util.List;


/**
 * SecurityBlockSerializer serializes a {@link AbstractSecurityBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class SecurityBlockSerializer {

    /**
     * serializes a {@link AbstractSecurityBlock}.
     *
     * @param block to serialize.
     * @return a Cbor-encoded serialized AbstractSecurityBlock.
     */
    static CborEncoder encode(AbstractSecurityBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(5)
                .cbor_start_array(block.securityTargets.size());

        for (Integer i : block.securityTargets) {
            enc.cbor_encode_int(i);
        }

        enc
                .cbor_encode_int(block.getCipherSuiteId())
                .cbor_encode_int(block.securityBlockFlag);

        if (block.getSaFlag(SecurityBlock.SecurityBlockFlags.SECURITY_SOURCE_PRESENT)) {
            enc.merge(EidSerializer.encode(block.securitySource));
        }

        enc.cbor_start_array(block.securityResults.size());
        for (List<SecurityResult> lsr : block.securityResults) {
            enc.cbor_start_array(lsr.size());
            for (SecurityResult sr : lsr) {
                enc.merge(sr.getValueEncoder());
            }
        }

        return enc;
    }

}
