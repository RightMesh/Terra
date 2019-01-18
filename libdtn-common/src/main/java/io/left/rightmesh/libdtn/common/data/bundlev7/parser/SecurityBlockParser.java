package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.SecurityBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

import java.util.LinkedList;

/**
 * SecurityBlockParser parses the data-specific part of an {@link SecurityBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class SecurityBlockParser {

    static CborParser getParser(AbstractSecurityBlock block, EidFactory eidFactory, Log logger) {
        return CBOR.parser()
                .cbor_open_array(5)
                .cbor_parse_linear_array(
                        CBOR.IntegerItem::new,
                        (p, t, i) -> {
                            logger.v(TAG, ".. nb_of_targets=" + i);
                        },
                        (p, t, item) -> {
                            logger.v(TAG, ".. target=" + item.value());
                            block.securityTargets.add((int) item.value());
                        },
                        (p, t, a) -> {
                        })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ".. cipherSuiteId=" + i);
                    block.cipherSuiteId = (int) i;
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ".. securityBlockFlag=" + i);
                    block.securityBlockFlag = (int) i;
                })
                .do_insert_if(
                        (p) -> block.getSaFlag(
                                SecurityBlock.SecurityBlockFlags.SECURITY_SOURCE_PRESENT),
                        CBOR.parser().cbor_parse_custom_item(
                                () -> new EidItem(eidFactory, logger),
                                (p, t, item) -> {
                                    logger.v(TAG, ".. securitySource="
                                            + item.eid.getEidString());
                                    block.securitySource = item.eid;
                                }))
                .cbor_parse_linear_array(
                        () ->   /* array in array */
                                () -> {
                                    logger.v(TAG, "... target="
                                            + block.securityResults.size());
                                    block.securityResults.add(new LinkedList<>());
                                    return CBOR.parser()
                                            .cbor_parse_linear_array(
                                                    () -> new SecurityResultItem(
                                                            block.cipherSuiteId,
                                                            block.securityResults.getLast().size(),
                                                            logger),
                                                    (p, t, size) ->
                                                            logger.v(TAG,
                                                                    "... target_results="
                                                                            + size),
                                                    (p, t, item) -> {
                                                        block.securityResults.getLast()
                                                                .add(item.securityResult);
                                                    },
                                                    (p, t, s) -> {
                                                    });
                                },
                        (p, t, size) -> logger.v(TAG, ".. security_results=" + size),
                        (p, t, item) -> {
                        },
                        (p, t, a) -> {
                        });
    }

}
