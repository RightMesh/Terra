package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.CRC;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class PrimaryBlockItem implements CborParser.ParseableItem {

    public PrimaryBlockItem(Log logger) {
        this.logger = logger;
    }

    public Bundle b;

    private Log logger;

    private CRC crc16;
    private CRC crc32;
    private boolean crc_ok = true;
    private CborParser close_crc = CBOR.parser().do_here(p -> p.set("crc_ok", false));

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .do_here((__) -> {
                    logger.v(TAG, ". preparing primary block CRC");
                    crc16 = CRC.init(CRC.CRCType.CRC16); // prepare CRC16 feeding
                    crc32 = CRC.init(CRC.CRCType.CRC32); // prepare CRC32 feeding
                })
                .do_for_each("crc-16", (__, buffer) -> crc16.read(buffer)) // feed CRC16 with every parsed buffer from this sequence
                .do_for_each("crc-32", (__, buffer) -> crc32.read(buffer)) // feed CRC32 with every parsed buffer from this sequence
                .cbor_open_array((__, ___, i) -> {
                    logger.v(TAG, ". array size=" + i);
                    if ((i < 8) || (i > 11)) {
                        throw new RxParserException("wrong number of element in primary block");
                    } else {
                        this.b = new Bundle();
                    }
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". version=" + i);
                    b.version = (int) i;
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". flags=" + i);
                    b.procV7Flags = i;
                })
                .cbor_parse_int((p, ___, i) -> {
                    logger.v(TAG, ". crc=" + i);
                    switch ((int) i) {
                        case 0:
                            b.crcType = PrimaryBlock.CRCFieldType.NO_CRC;
                            p.undo_for_each_now("crc-16");
                            p.undo_for_each_now("crc-32");
                            break;
                        case 1:
                            b.crcType = PrimaryBlock.CRCFieldType.CRC_16;
                            p.undo_for_each_now("crc-32");
                            close_crc = crc16CloseParser();
                            break;
                        case 2:
                            b.crcType = PrimaryBlock.CRCFieldType.CRC_32;
                            p.undo_for_each_now("crc-16");
                            close_crc = crc32CloseParser();
                            break;
                        default:
                            throw new RxParserException("wrong CRC type");
                    }
                })
                .cbor_parse_custom_item(() -> new EIDItem(logger), (__, ___, item) -> {
                    logger.v(TAG, ". destination=" + item.eid.getEIDString());
                    b.destination = item.eid;
                })
                .cbor_parse_custom_item(() -> new EIDItem(logger), (__, ___, item) -> {
                    logger.v(TAG, ". source=" + item.eid.getEIDString());
                    b.source = item.eid;
                })
                .cbor_parse_custom_item(() -> new EIDItem(logger), (__, ___, item) -> {
                    logger.v(TAG, ". reportto=" + item.eid.getEIDString());
                    b.reportto = item.eid;
                })
                .cbor_open_array(2)
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". creationTimestamp=" + i);
                    b.creationTimestamp = i;
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". sequenceNumber=" + i);
                    b.sequenceNumber = i;
                    b.bid = BundleID.create(b.source, b.creationTimestamp, b.sequenceNumber);
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". lifetime=" + i);
                    b.lifetime = i;
                })
                .do_here(p -> p.insert_now(close_crc)) // validate close_crc
                .do_here(p -> {
                    logger.v(TAG, ". crc_check=" + crc_ok);
                    b.tag("crc_check", crc_ok);
                }); // tag the block
    }


    private CborParser crc16CloseParser() {
        return CBOR.parser()
                .undo_for_each("crc-16", (p) -> {
                    byte[] zeroCRC = {0x42, 0x00, 0x00};
                    crc16.read(ByteBuffer.wrap(zeroCRC));
                })
                .cbor_parse_byte_string(
                        (__, ____, s) -> {
                            if (s != 2) {
                                throw new RxParserException("CRC 16 should be exactly 2 bytes");
                            }
                        },
                        (p, buffer) -> {
                            crc_ok = crc16.doneAndValidate(buffer);
                        });
    }

    private CborParser crc32CloseParser() {
        return CBOR.parser()
                .undo_for_each("crc-32", (__) -> {
                    byte[] zeroCRC = {0x44, 0x00, 0x00, 0x00, 0x00};
                    crc32.read(ByteBuffer.wrap(zeroCRC));
                })
                .cbor_parse_byte_string(
                        (__, ____, s) -> {
                            if (s != 4) {
                                throw new RxParserException("CRC 32 should be exactly 4 bytes");
                            }
                        },
                        (p, buffer) -> {
                            crc_ok = crc32.doneAndValidate(buffer);
                        });
    }


}
