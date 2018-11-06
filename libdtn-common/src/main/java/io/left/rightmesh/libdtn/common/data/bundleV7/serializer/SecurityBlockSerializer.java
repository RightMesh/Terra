package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import java.util.List;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityResult;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class SecurityBlockSerializer  {

    static CborEncoder encode(AbstractSecurityBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(5)
                .cbor_start_array(block.securityTargets.size());

        for(Integer i : block.securityTargets) {
            enc.cbor_encode_int(i);
        }

        enc
                .cbor_encode_int(block.getCipherSuiteId())
                .cbor_encode_int(block.securityBlockFlag);

        if(block.getSAFlag(SecurityBlock.SecurityBlockFlags.SECURITY_SOURCE_PRESENT)) {
                enc.merge(EIDSerializer.encode(block.securitySource));
        }

        enc.cbor_start_array(block.securityResults.size());
        for(List<SecurityResult> lsr : block.securityResults) {
            enc.cbor_start_array(lsr.size());
            for(SecurityResult sr : lsr) {
                enc.merge(sr.getValueEncoder());
            }
        }

        return enc;
    }

}
