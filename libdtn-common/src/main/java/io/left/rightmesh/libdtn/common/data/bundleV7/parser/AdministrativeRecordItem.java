package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.AdministrativeRecord;
import io.left.rightmesh.libdtn.common.data.StatusReport;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 10/11/18.
 */
public class AdministrativeRecordItem implements CborParser.ParseableItem {

    static final String TAG = "AdministrativeRecordItem";

    public AdministrativeRecordItem(Log logger) {
        this.logger = logger;
    }

    public AdministrativeRecord record;

    private Log logger;
    private CborParser body;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array((__, ___, i) -> {
                    logger.v(TAG, ". array size=" + i);
                    if (i != 2) {
                        throw new RxParserException("wrong number of element in canonical block");
                    }
                })
                .cbor_parse_int((p, __, i) -> { // block type
                    logger.v(TAG, ". type=" + i);
                    switch ((int) i) {
                        case StatusReport.type:
                            record = new StatusReport();
                            body = StatusReportParser.getParser((StatusReport) record, logger);
                            break;
                        default:
                            throw new RxParserException("administrative record type unknown: "+i);
                    }
                })
                .do_here(p -> p.insert_now(body));
    }

}
