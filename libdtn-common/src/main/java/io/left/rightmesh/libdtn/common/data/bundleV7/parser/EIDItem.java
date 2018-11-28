package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.common.data.eid.IPN;
import io.left.rightmesh.libdtn.common.data.eid.UnknowEID;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;
import static io.left.rightmesh.libdtn.common.data.eid.DTN.EID_DTN_IANA_VALUE;
import static io.left.rightmesh.libdtn.common.data.eid.IPN.EID_IPN_IANA_VALUE;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class EIDItem implements CborParser.ParseableItem {

    public EIDItem(EIDFactory eidFactory, Log logger) {
        this.eidFactory = eidFactory;
        this.logger = logger;
    }

    private EIDFactory eidFactory;
    private Log logger;

    public EID eid;
    public int iana_number;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((p, __, i) -> {
                    logger.v(TAG, ".. iana_value=" + i);
                    this.iana_number = (int) i;
                    switch ((int) i) {
                        case EID_IPN_IANA_VALUE:
                            p.insert_now(parseIPN);
                            break;
                        case EID_DTN_IANA_VALUE:
                            p.insert_now(parseDTN);
                            break;
                        default:
                            p.insert_now(parseEID);
                    }
                });
    }

    private CborParser parseIPN = CBOR.parser()
            .cbor_open_array(2)
            .cbor_parse_int((___, ____, node) -> eid = new IPN((int) node, 0))
            .cbor_parse_int((___, ____, service) -> ((IPN) eid).service_number = (int) service);

    private CborParser parseDTN = CBOR.parser()
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
                                } catch (EIDFormatException efe) {
                                    throw new RxParserException("DTN is not an URI: " + efe);
                                }
                            }));

    private CborParser parseEID = CBOR.parser()
            .cbor_parse_text_string_full(
                    (__, ___, size) -> {
                        if (size > 1024) {
                            throw new RxParserException("EID size should not exceed 1024");
                        }
                    },
                    (__, ssp) -> {
                        try {
                            String scheme = eidFactory.getIANAScheme(iana_number);
                            eid = eidFactory.create(scheme, ssp);
                            logger.v(TAG, ".. eid scheme=" + scheme + " ssp=" + ssp);
                        } catch (EIDFactory.UnknownIanaNumber | EIDFactory.UnknownEIDScheme uin) {
                            logger.v(TAG, ".. unknown EID=" + iana_number + " ssp=" + ssp);
                            try {
                                eid = new UnknowEID(iana_number, ssp);
                            } catch (EIDFormatException efe) {
                                throw new RxParserException("error parsing EID: " + efe.getMessage());
                            }
                        } catch (EIDFormatException efe) {
                            throw new RxParserException("error parsing EID: " + efe.getMessage());
                        }
                    });
}
