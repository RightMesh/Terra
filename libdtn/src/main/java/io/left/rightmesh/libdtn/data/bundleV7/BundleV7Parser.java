package io.left.rightmesh.libdtn.data.bundleV7;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.CborParser.ExpectedType;
import io.left.rightmesh.libcbor.rxparser.ParserEmitter;
import io.left.rightmesh.libcbor.rxparser.ParserState;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.CRC;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.data.ManifestBlock;
import io.left.rightmesh.libdtn.data.PayloadBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.data.UnknownExtensionBlock;
import io.reactivex.Observer;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Parser extends ParserEmitter<Bundle> {

    private CborParser bundleParser;

    public BundleV7Parser(Observer<? super Bundle> downstream) {
        super(downstream);
    }

    @Override
    public void onReset() {
        bundleParser = CBOR.parser()
                .cbor_parse_custom_item(BundleItem::new, (p, __, item) -> emit(item.bundle));
    }

    @Override
    public ParserState initState() {
        onReset();
        return parseBundle;
    }

    private ParserState parseBundle = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (bundleParser.read(next)) {
                return initState();
            }
            return this;
        }
    };

    public class BundleItem implements CborParser.ParseableItem {

        Bundle bundle = null;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array((__, ___, ____) -> {
                    })
                    .cbor_parse_custom_item(PrimaryBlockItem::new, (p, __, item) -> {
                        bundle = item.b;
                    })
                    .cbor_parse_array_items(CanonicalBlockItem::new, (p, __, item) -> {
                        bundle.addBlock(item.block);
                    });
        }
    }

    public class PrimaryBlockItem extends BlockWithCRC {

        Bundle b;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .do_here((__) -> {
                        crc16 = CRC.init(CRC.CRCType.CRC16); // prepare CRC16 feeding
                        crc32 = CRC.init(CRC.CRCType.CRC32); // prepare CRC32 feeding
                    })
                    .do_for_each("crc-16", (__, buffer) -> crc16.read(buffer))
                    .do_for_each("crc-32", (__, buffer) -> crc32.read(buffer))
                    .cbor_open_array((__, ___, i) -> {
                        if ((i < 8) || (i > 11)) {
                            throw new RxParserException("wrong number of element in primary block");
                        } else {
                            this.b = new Bundle();
                        }
                    })
                    .cbor_parse_int((__, ___, i) -> b.version = (int) i)
                    .cbor_parse_int((__, ___, i) -> b.procV7Flags = i)
                    .cbor_parse_int((p, ___, i) -> {
                        switch ((int) i) {
                            case 0:
                                b.crcType = PrimaryBlock.CRCFieldType.NO_CRC;
                                p.undo_for_each("crc-16");
                                p.undo_for_each("crc-32");
                                crc = CBOR.parser();
                                break;
                            case 1:
                                b.crcType = PrimaryBlock.CRCFieldType.CRC_16;
                                p.undo_for_each("crc-32");
                                crc = crc32Parser;
                                break;
                            case 2:
                                b.crcType = PrimaryBlock.CRCFieldType.CRC_32;
                                p.undo_for_each("crc-16");
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                        //crc_type(b.crcType);
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.destination = item.eid)
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.source = item.eid)
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.reportto = item.eid)
                    .cbor_open_array(2)
                    .cbor_parse_int((__, ___, i) -> b.creationTimestamp = i)
                    .cbor_parse_int((__, ___, i) -> b.sequenceNumber = i)
                    .cbor_parse_int((__, ___, i) -> b.lifetime = i)
                    .do_here(p -> p.insert(crc));
        }
    }


    public class CanonicalBlockItem extends BlockWithCRC {

        Block block;

        CborParser payload;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .do_here((__) -> {
                        crc16 = CRC.init(CRC.CRCType.CRC16); // prepare CRC16 feeding
                        crc32 = CRC.init(CRC.CRCType.CRC32); // prepare CRC32 feeding
                    })
                    .do_for_each("crc-16", (__, buffer) -> crc16.read(buffer))
                    .do_for_each("crc-32", (__, buffer) -> crc32.read(buffer))
                    .cbor_open_array((__, ___, i) -> {
                        if ((i != 5) && (i != 6)) {
                            throw new RxParserException("wrong number of element in canonical block");
                        }
                    })
                    .cbor_parse_int((p, __, i) -> { // block type
                        switch ((int) i) {
                            case 0:
                                block = new PayloadBlock();
                                payload = blobBlock;
                                break;
                            case 2:
                                block = new BlockIntegrityBlock();
                                payload = blockIntegrityBlock;
                                break;
                            case 4:
                                block = new ManifestBlock();
                                payload = manifestBlock;
                                break;
                            case 6:
                                block = new FlowLabelBlock();
                                payload = flowLabelBlock;
                                break;
                            case 7:
                                block = new PreviousNodeBlock();
                                payload = previousNodeBlock;
                                break;
                            case 8:
                                block = new AgeBlock();
                                payload = ageBlock;
                                break;
                            case 9:
                                block = new ScopeControlHopLimitBlock();
                                payload = scopeControlLimitBlock;
                                break;
                            default:
                                block = new UnknownExtensionBlock((int) i);
                                payload = blobBlock;
                                break;
                        }
                    })
                    .cbor_parse_int((__, ___, i) -> block.number = (int) i)
                    .cbor_parse_int((__, ___, i) -> block.procV7flags = i)
                    .cbor_parse_int((p, ___, i) -> {
                        switch ((int) i) {
                            case 0:
                                block.crcType = BlockHeader.CRCFieldType.NO_CRC;
                                p.undo_for_each("crc-16"); // deactivate CRC16 feeding
                                p.undo_for_each("crc-32"); // deactivate CRC32 feeding
                                break;
                            case 1:
                                block.crcType = BlockHeader.CRCFieldType.CRC_16;
                                p.undo_for_each("crc-32"); // deactivate CRC32 feeding
                                crc = crc16Parser;
                                break;
                            case 2:
                                block.crcType = BlockHeader.CRCFieldType.CRC_32;
                                p.undo_for_each("crc-16"); // deactivate CRC16 feeding
                                crc = crc32Parser;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .do_here(p -> p.insert(payload))
                    .do_here(p -> p.insert(crc));
        }

        CborParser blobBlock = CBOR.parser();

        CborParser blockIntegrityBlock = CBOR.parser();

        CborParser manifestBlock = CBOR.parser();

        CborParser flowLabelBlock = CBOR.parser();

        CborParser previousNodeBlock = CBOR.parser();

        CborParser ageBlock = CBOR.parser();

        CborParser scopeControlLimitBlock = CBOR.parser();
    }


    public abstract class BlockWithCRC implements CborParser.ParseableItem {
        CborParser crc;

        CRC crc16;
        CRC crc32;

        CborParser crc16Parser = CBOR.parser()
                .undo_for_each("crc-16", (__) -> {
                    byte[] zeroCRC = {0x42, 0x00, 0x00};
                    crc16.read(ByteBuffer.wrap(zeroCRC));
                })
                .cbor_parse_byte_string(
                        (__, ____, s) -> {
                            if (s != 2) {
                                throw new RxParserException("CRC 16 should be exactly 2 bytes");
                            }
                        },
                        (__, buffer) -> {
                            if (!crc16.doneAndValidate(buffer)) {
                                // taint the block
                            }
                        });

        CborParser crc32Parser = CBOR.parser()
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
                        (__, buffer) -> {
                            if (!crc32.doneAndValidate(buffer)) {
                                // taint the block
                            }
                        });
    }

    public class EIDItem implements CborParser.ParseableItem {

        public EID eid;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_int((p, __, i) -> {
                        if (i == 0) { // IPN
                            p.insert(parseIPN);
                        } else { // DTN or UNKNOWN
                            p.insert(parseDTN);
                        }
                    });
        }

        CborParser parseIPN = CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((___, ____, node) -> eid = EID.createIPN((int) node, 0))
                .cbor_parse_int((___, ____, service) -> ((EID.IPN) eid).service_number = (int) service);

        CborParser parseDTN = CBOR.parser()
                .cbor_or(
                        CBOR.parser().cbor_parse_int((___, ____, i) -> {
                            eid = EID.NullEID();
                        }),
                        CBOR.parser().cbor_parse_text_string_full(
                                (__, ___, size) -> {
                                    if (size > 1024) {
                                        throw new RxParserException("EID size should not exceed 1024");
                                    }
                                },
                                (__, str) -> {
                                    eid = EID.createDTN(str);
                                }));
    }
}
