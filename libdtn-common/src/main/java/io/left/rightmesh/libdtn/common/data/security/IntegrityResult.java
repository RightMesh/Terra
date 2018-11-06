package io.left.rightmesh.libdtn.common.data.security;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;

/**
 * @author Lucien Loiseau on 06/11/18.
 */
public class IntegrityResult implements SecurityResult {

    public static final int resultId = 1;

    byte[] checksum;

    public IntegrityResult(byte[] checksum) {
        this.checksum = checksum;
    }

    @Override
    public int getResultId() {
        return resultId;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    @Override
    public CborEncoder getValueEncoder() {
        return CBOR.encoder()
                .cbor_encode_byte_string(getChecksum());
    }
}
