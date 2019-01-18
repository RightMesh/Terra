package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;
import static io.left.rightmesh.libdtn.common.data.eid.DtnEid.EID_DTN_IANA_VALUE;
import static io.left.rightmesh.libdtn.common.data.eid.EidIpn.EID_IPN_IANA_VALUE;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
import io.left.rightmesh.libdtn.common.data.eid.EidIpn;
import io.left.rightmesh.libdtn.common.data.eid.UnknowEid;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * EidItem is a CborParser.ParseableItem for an {@link Eid}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class EidItem implements CborParser.ParseableItem {

    public EidItem(EidFactory eidFactory, Log logger) {
        this.eidFactory = eidFactory;
        this.logger = logger;
    }

    private EidFactory eidFactory;
    private Log logger;

    public Eid eid;
    public int ianaNumber;

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ".. iana_value=" + i);
                    this.ianaNumber = (int) i;
                    switch ((int) i) {
                        case EID_IPN_IANA_VALUE:
                            p.insert_now(parseIpn);
                            break;
                        case EID_DTN_IANA_VALUE:
                            p.insert_now(parseDtn);
                            break;
                        default:
                            p.insert_now(parseEid);
                    }
                });
    }

    private CborParser parseIpn = CBOR.parser()
            .cbor_open_array(2)
            .cbor_parse_int((p, t, node) -> eid = new EidIpn((int) node, 0))
            .cbor_parse_int((p, t, service) -> ((EidIpn) eid).serviceNumber = (int) service);

    private CborParser parseDtn = CBOR.parser()
            .cbor_or(
                    CBOR.parser().cbor_parse_int((p, t, i) -> {
                        eid = DtnEid.nullEid();
                    }),
                    CBOR.parser().cbor_parse_text_string_full(
                            (p, t, size) -> {
                                if (size > 1024) {
                                    throw new RxParserException("Eid size should not exceed 1024");
                                }
                            },
                            (p, str) -> {
                                logger.v(TAG, ".. dtn_ssp=" + str);
                                try {
                                    eid = new DtnEid(str);
                                } catch (EidFormatException efe) {
                                    throw new RxParserException("DtnEid is not an URI: " + efe);
                                }
                            }));

    private CborParser parseEid = CBOR.parser()
            .cbor_parse_text_string_full(
                    (p, t, size) -> {
                        if (size > 1024) {
                            throw new RxParserException("Eid size should not exceed 1024");
                        }
                    },
                    (p, ssp) -> {
                        try {
                            String scheme = eidFactory.getIanaScheme(ianaNumber);
                            eid = eidFactory.create(scheme, ssp);
                            logger.v(TAG, ".. eid scheme=" + scheme + " ssp=" + ssp);
                        } catch (EidFactory.UnknownIanaNumber | EidFactory.UnknownEidScheme uin) {
                            logger.v(TAG, ".. unknown Eid=" + ianaNumber + " ssp=" + ssp);
                            try {
                                eid = new UnknowEid(ianaNumber, ssp);
                            } catch (EidFormatException efe) {
                                throw new RxParserException("error parsing Eid: "
                                        + efe.getMessage());
                            }
                        } catch (EidFormatException efe) {
                            throw new RxParserException("error parsing Eid: " + efe.getMessage());
                        }
                    });
}
