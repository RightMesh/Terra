package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityResult;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class SecurityBlockParser {

    static CborParser getParser(AbstractSecurityBlock block, EIDFactory eidFactory, Log logger) {
        return CBOR.parser()
                .cbor_open_array(5)
                .cbor_parse_linear_array(
                        CBOR.IntegerItem::new,
                        (__, ___, i) -> {
                            logger.v(TAG, ".. nb_of_targets=" + i);
                        },
                        (__, ___, item) -> {
                            logger.v(TAG, ".. target=" + item.value());
                            block.securityTargets.add((int) item.value());
                        },
                        (__, ___, ____) -> {
                        })
                .cbor_parse_int((p, __, i) -> {
                    logger.v(TAG, ".. cipherSuiteId=" + i);
                    block.cipherSuiteId = (int) i;
                })
                .cbor_parse_int((p, __, i) -> {
                    logger.v(TAG, ".. securityBlockFlag=" + i);
                    block.securityBlockFlag = (int) i;
                })
                .do_insert_if(
                        (p) -> block.getSAFlag(SecurityBlock.SecurityBlockFlags.SECURITY_SOURCE_PRESENT),
                        CBOR.parser().cbor_parse_custom_item(
                                () -> new EIDItem(eidFactory, logger),
                                (p, __, item) -> {
                                    logger.v(TAG, ".. securitySource=" + item.eid.getEIDString());
                                    block.securitySource = item.eid;
                                }))
                .cbor_parse_linear_array(
                        () ->   /* array in array */
                                () -> {
                                    logger.v(TAG, "... target=" + block.securityResults.size());
                                    block.securityResults.add(new LinkedList<>());
                                    return CBOR.parser()
                                            .cbor_parse_linear_array(
                                                    () -> new SecurityResultItem(block.cipherSuiteId, block.securityResults.getLast().size(), logger),
                                                    (p, __, size) -> logger.v(TAG, "... target_results=" + size),
                                                    (__, ___, item) -> {
                                                        block.securityResults.getLast().add(item.securityResult);
                                                    },
                                                    (p, __, ___) -> { });
                                },
                        (__, ___, size) -> logger.v(TAG, ".. security_results=" + size),
                        (__, ___, item) -> { },
                        (__, ___, ____) -> { });
    }

}
