package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;

/**
 * IntegrityResult is a SecurityResult for Integrity operations.
 *
 * @author Lucien Loiseau on 06/11/18.
 */
public class IntegrityResult implements SecurityResult {

    private static final int INTEGRITY_RESULT_ID = 1;

    private byte[] checksum;

    public IntegrityResult(byte[] checksum) {
        this.checksum = checksum;
    }

    @Override
    public int getResultId() {
        return INTEGRITY_RESULT_ID;
    }

    /**
     * return the result of the checksum as a byte array.
     *
     * @return byte array holding the checksum.
     */
    public byte[] getChecksum() {
        return checksum;
    }

    @Override
    public CborEncoder getValueEncoder() {
        return CBOR.encoder()
                .cbor_encode_byte_string(getChecksum());
    }
}
