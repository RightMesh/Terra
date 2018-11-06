package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.security.IntegrityResult;
import io.left.rightmesh.libdtn.common.data.security.SecurityResult;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;
import static io.left.rightmesh.libdtn.common.data.security.CipherSuites.BIB_SHA256;

/**
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
        if(cipherId == BIB_SHA256.getId()) {
            return CBOR.parser()
                    .cbor_parse_byte_string(
                            (__, ___, size) -> {
                                /* size of the checksum */
                            },
                            (__, chunk) -> {
                                securityResult = new IntegrityResult(chunk.array());
                                logger.v(TAG, ".... result_id=" + securityResult.getResultId());
                                logger.v(TAG, ".... result_value=" + new String(chunk.array()));
                            },
                            (__) -> {
                            });


        }
        return CBOR.parser();
    }
}
