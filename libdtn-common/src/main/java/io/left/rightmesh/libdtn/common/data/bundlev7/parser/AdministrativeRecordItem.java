package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.RxParserException;
import io.left.rightmesh.libdtn.common.data.AdministrativeRecord;
import io.left.rightmesh.libdtn.common.data.StatusReport;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * Parser for an {@link AdministrativeRecord}.
 *
 * @author Lucien Loiseau on 10/11/18.
 */
public class AdministrativeRecordItem implements ParseableItem {

    static final String TAG = "AdministrativeRecordItem";

    public AdministrativeRecordItem(EidFactory eidFactory, Log logger) {
        this.logger = logger;
    }

    public AdministrativeRecord record;

    private Log logger;
    private EidFactory eidFactory;
    private CborParser body;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array((parser, tags, i) -> {
                    logger.v(TAG, ". array size=" + i);
                    if (i != 2) {
                        throw new RxParserException("wrong number of element in canonical block");
                    }
                })
                .cbor_parse_int((parser, tags, i) -> { // block type
                    logger.v(TAG, ". adm type=" + i);
                    switch ((int) i) {
                        case StatusReport.STATUS_REPORT_ADM_TYPE:
                            record = new StatusReport();
                            body = StatusReportParser
                                    .getParser((StatusReport) record, eidFactory, logger);
                            break;
                        default:
                            throw new RxParserException("administrative record type unknown: " + i);
                    }
                })
                .do_here(p -> p.insert_now(body));
    }

}
