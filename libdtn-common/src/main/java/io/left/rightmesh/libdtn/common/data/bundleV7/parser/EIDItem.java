package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.eid.API;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.IPN;
import io.left.rightmesh.libdtn.common.data.eid.UnkownEID;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class EIDItem implements CborParser.ParseableItem {

    public EIDItem(Log logger) {
        this.logger = logger;
    }

    private Log logger;

    public EID eid;
    public int unknown_iana_number;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((p, __, i) -> {
                    logger.v(TAG, ".. iana_value=" + i);
                    switch ((int) i) {
                        case EID.EID_IPN_IANA_VALUE:
                            p.insert_now(parseIPN);
                            break;
                        case EID.EID_DTN_IANA_VALUE:
                            p.insert_now(parseDTN);
                            break;
                        case EID.EID_CLA_IANA_VALUE:
                            p.insert_now(parseCLA);
                            break;
                        case EID.EID_API_ME:
                            p.insert_now(parseAPI);
                            break;
                        default:
                            p.insert_now(parseUNK);
                    }
                });
    }

    CborParser parseIPN = CBOR.parser()
            .cbor_open_array(2)
            .cbor_parse_int((___, ____, node) -> eid = new IPN((int) node, 0))
            .cbor_parse_int((___, ____, service) -> ((IPN) eid).service_number = (int) service);

    CborParser parseDTN = CBOR.parser()
            .cbor_or(
                    CBOR.parser().cbor_parse_int((___, ____, i) -> {
                        eid = DTN.NullEID();
                    }),
                    CBOR.parser().cbor_parse_text_string_full(
                            (__, ___, size) -> {
                                if (size > 1024) {
                                    throw new RxParserException("EID size should not exceed 1024");
                                }
                            },
                            (__, str) -> {
                                logger.v(TAG, ".. dtn_ssp=" + str);
                                try {
                                    eid = new DTN(str);
                                } catch (EID.EIDFormatException efe) {
                                    throw new RxParserException("DTN is not an URI: " + efe);
                                }
                            }));

    CborParser parseCLA = CBOR.parser()
            .cbor_parse_text_string_full(
                    (__, ___, size) -> {
                        if (size > 1024) {
                            throw new RxParserException("EID size should not exceed 1024");
                        }
                    },
                    (__, str) -> {
                        try {
                            logger.v(TAG, ".. cla_ssp=" + str);
                            eid = CLA.create(str);
                        } catch (EID.EIDFormatException efe) {
                            throw new RxParserException("not a CLA EID: " + efe.getMessage());
                        }
                    });

    CborParser parseAPI = CBOR.parser()
            .cbor_parse_text_string_full(
                    (__, ___, size) -> {
                        if (size > 1024) {
                            throw new RxParserException("EID size should not exceed 1024");
                        }
                    },
                    (__, str) -> {
                        logger.v(TAG, ".. api_ssp=" + str);
                        try {
                            eid = new API(str);
                        } catch (EID.EIDFormatException efe) {
                            throw new RxParserException("API eid format exception: " + efe.getMessage());
                        }
                    });

    CborParser parseUNK = CBOR.parser()
            .cbor_parse_text_string_full(
                    (__, ___, size) -> {
                        if (size > 1024) {
                            throw new RxParserException("EID size should not exceed 1024");
                        }
                    },
                    (__, str) -> {
                        try {
                            logger.v(TAG, ".. unk_ssp=" + str);
                            eid = new UnkownEID(unknown_iana_number, "unk", str);
                        } catch (EID.EIDFormatException efe) {
                            throw new RxParserException("unknown EID: " + efe.getMessage());
                        }
                    });
}
