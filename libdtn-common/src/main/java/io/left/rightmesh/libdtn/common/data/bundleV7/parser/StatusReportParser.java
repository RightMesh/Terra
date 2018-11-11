package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.StatusReport;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.AdministrativeRecordItem.TAG;

/**
 * @author Lucien Loiseau on 10/11/18.
 */
public class StatusReportParser {

    static CborParser getParser(StatusReport report, Log logger) {
        return CBOR.parser()
                .cbor_open_array((__, ___, i) -> {
                    logger.v(TAG, ".. status_report_array size=" + i);
                    if (i != 4 && i != 6) {
                        throw new RxParserException("wrong number of element in status report");
                    }
                    if (i == 6) {
                        report.subjectBundleIsFragment = true;
                    }
                })
                .cbor_parse_linear_array(
                        () ->   /* array in array */
                                () -> { /* status assertion item: [true, timestamp] or [false] */
                                    StatusReport.StatusAssertion assertion = StatusReport.StatusAssertion.values()[report.statusInformation.size()];
                                    return CBOR.parser()
                                            .cbor_open_array((p, ___, i) -> {
                                                if (i == 1) {
                                                    p.insert_now(CBOR.parser().cbor_parse_boolean((__, b) -> {
                                                        logger.v(TAG, ".... " + assertion + "=false");
                                                    }));
                                                } else if (i == 2) {
                                                    p.insert_now(CBOR.parser()
                                                            .cbor_parse_boolean((__, b) -> {
                                                                logger.v(TAG, ".... " + assertion + "=true");
                                                            })
                                                            .cbor_parse_int((__, ____, timestamp) -> {
                                                                logger.v(TAG, ".... timestamp=true");
                                                                report.statusInformation.put(assertion, timestamp);
                                                            })
                                                    );
                                                } else {
                                                    throw new RxParserException("wrong number of element in status report");
                                                }
                                            });
                                },
                        (__, ___, size) -> {
                            logger.v(TAG, "... status_assertion_array_size=" + size);
                            if (size != StatusReport.StatusAssertion.values().length) {
                                throw new RxParserException("wrong number of status assertion");
                            }
                        },
                        (__, ___, item) -> { /* ignore, already dealt with in item factory */
                        },
                        (__, ___, ____) -> { /* ignore, already dealt with in item factory */
                        })
                .cbor_parse_int((__, ____, error) -> {
                    logger.v(TAG, ".. error_code="+error);
                    if(error > StatusReport.ReasonCode.values().length) {
                        report.code = StatusReport.ReasonCode.Other;
                    } else {
                        report.code = StatusReport.ReasonCode.values()[(int) error];
                    }
                })
                .cbor_parse_custom_item(() -> new EIDItem(logger), (__, ___, item) -> {
                    logger.v(TAG, ".. subject_source_EID=" + item.eid.getEIDString());
                    report.source = item.eid;
                })
                .cbor_parse_int((__, ____, timestamp) -> {
                    logger.v(TAG, ".. subbject_creation_timestamp="+timestamp);
                    report.creationTimestamp = timestamp;
                }); //todo fragmented bundle is not supported
    }

}
