package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;
import static io.left.rightmesh.libdtn.common.data.security.CipherSuites.BIB_SHA256;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.security.IntegrityResult;
import io.left.rightmesh.libdtn.common.data.security.SecurityResult;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * SecurityResultItem is a CborParser.ParseableItem for a SecurityResult.
 *
 * @author Lucien Loiseau on 06/11/18.
 */
public class SecurityResultItem implements CborParser.ParseableItem {

    SecurityResultItem(int cipherId, int resultId, Log logger) {
        this.cipherId = cipherId;
        this.resultId = resultId;
        this.logger = logger;
    }

    public SecurityResult securityResult;

    private int cipherId;
    private int resultId;
    private Log logger;

    @Override
    public CborParser getItemParser() {
        if (cipherId == BIB_SHA256.getId()) {
            return CBOR.parser()
                    .cbor_parse_byte_string(
                            (p, t, size) -> {
                                /* size of the checksum */
                            },
                            (p, chunk) -> {
                                securityResult = new IntegrityResult(chunk.array());
                                logger.v(TAG, ".... result_id="
                                        + securityResult.getResultId());
                                logger.v(TAG, ".... result_value="
                                        + new String(chunk.array()));
                            },
                            (p) -> {
                            });


        }
        return CBOR.parser();
    }
}
