package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.Crc;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

import java.nio.ByteBuffer;

/**
 * PrimaryBlockItem is a CborParser.ParseableItem for {@link PrimaryBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class PrimaryBlockItem implements CborParser.ParseableItem {

    public PrimaryBlockItem(EidFactory eidFactory, Log logger) {
        this.eidFactory = eidFactory;
        this.logger = logger;
    }

    private EidFactory eidFactory;
    private Log logger;

    public Bundle bundle;

    private Crc crc16;
    private Crc crc32;
    private boolean crcOk = true;
    private CborParser closeCrc = CBOR.parser().do_here(p -> p.set("crcOk", false));

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .do_here((p) -> {
                    logger.v(TAG, ". preparing primary block Crc");
                    crc16 = Crc.init(Crc.CrcType.CRC16); // prepare Crc16 feeding
                    crc32 = Crc.init(Crc.CrcType.CRC32); // prepare Crc32 feeding
                })
                .do_for_each("crc-16", (p, buffer) -> crc16.read(buffer)) // feed Crc16
                .do_for_each("crc-32", (p, buffer) -> crc32.read(buffer)) // feed Crc32
                .cbor_open_array((p, t, i) -> {
                    logger.v(TAG, ". array size=" + i);
                    if ((i < 8) || (i > 11)) {
                        throw new RxParserException("wrong number of element in primary block");
                    } else {
                        this.bundle = new Bundle();
                    }
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". version=" + i);
                    bundle.setVersion((int) i);
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". flags=" + i);
                    bundle.setProcV7Flags(i);
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". crc=" + i);
                    switch ((int) i) {
                        case 0:
                            bundle.setCrcType(PrimaryBlock.CrcFieldType.NO_CRC);
                            p.undo_for_each_now("crc-16");
                            p.undo_for_each_now("crc-32");
                            break;
                        case 1:
                            bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_16);
                            p.undo_for_each_now("crc-32");
                            closeCrc = crc16CloseParser();
                            break;
                        case 2:
                            bundle.setCrcType(PrimaryBlock.CrcFieldType.CRC_32);
                            p.undo_for_each_now("crc-16");
                            closeCrc = crc32CloseParser();
                            break;
                        default:
                            throw new RxParserException("wrong Crc PAYLOAD_BLOCK_TYPE");
                    }
                })
                .cbor_parse_custom_item(() -> new EidItem(eidFactory, logger), (p, t, item) -> {
                    logger.v(TAG, ". destination=" + item.eid.getEidString());
                    bundle.setDestination(item.eid);
                })
                .cbor_parse_custom_item(() -> new EidItem(eidFactory, logger), (p, t, item) -> {
                    logger.v(TAG, ". source=" + item.eid.getEidString());
                    bundle.setSource(item.eid);
                })
                .cbor_parse_custom_item(() -> new EidItem(eidFactory, logger), (p, t, item) -> {
                    logger.v(TAG, ". reportto=" + item.eid.getEidString());
                    bundle.setReportto(item.eid);
                })
                .cbor_open_array(2)
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". creationTimestamp=" + i);
                    bundle.setCreationTimestamp(i);
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". sequenceNumber=" + i);
                    bundle.setSequenceNumber(i);
                    logger.v(TAG, ". bid=" + bundle.getBid().getBidString());
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". lifetime=" + i);
                    bundle.setLifetime(i);
                })
                .do_here(p -> p.insert_now(closeCrc)) // validate closeCrc
                .do_here(p -> {
                    logger.v(TAG, ". crc_check=" + crcOk);
                    bundle.tag("crc_check", crcOk);
                }); // tag the block
    }


    private CborParser crc16CloseParser() {
        return CBOR.parser()
                .undo_for_each("crc-16", (p) -> {
                    byte[] zeroCrc = {0x42, 0x00, 0x00};
                    crc16.read(ByteBuffer.wrap(zeroCrc));
                })
                .cbor_parse_byte_string(
                        (p, t, s) -> {
                            if (s != 2) {
                                throw new RxParserException("Crc 16 should be exactly 2 bytes");
                            }
                        },
                        (p, buffer) -> {
                            crcOk = crc16.doneAndValidate(buffer);
                        });
    }

    private CborParser crc32CloseParser() {
        return CBOR.parser()
                .undo_for_each("crc-32", (p) -> {
                    byte[] zeroCrc = {0x44, 0x00, 0x00, 0x00, 0x00};
                    crc32.read(ByteBuffer.wrap(zeroCrc));
                })
                .cbor_parse_byte_string(
                        (p, t, s) -> {
                            if (s != 4) {
                                throw new RxParserException("Crc 32 should be exactly 4 bytes");
                            }
                        },
                        (p, buffer) -> {
                            crcOk = crc32.doneAndValidate(buffer);
                        });
    }


}
