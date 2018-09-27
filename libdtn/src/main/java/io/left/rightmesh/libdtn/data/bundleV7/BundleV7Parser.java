package io.left.rightmesh.libdtn.data.bundleV7;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockBLOB;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.CRC;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.data.ManifestBlock;
import io.left.rightmesh.libdtn.data.PayloadBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.BundleStorage;
import io.left.rightmesh.libdtn.storage.NullBLOB;
import io.left.rightmesh.libdtn.storage.WritableBLOB;
import io.reactivex.Observer;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Parser  {

    public interface BundleParsedCallback {
        void onBundleParsed(Bundle b);
    }

    public static CborParser create(BundleParsedCallback cb) {
        return CBOR.parser().cbor_parse_custom_item(BundleItem::new, (__, ___, item) -> cb.onBundleParsed(item.bundle));
    }

    public static class BundleItem implements CborParser.ParseableItem {

        public Bundle bundle = null;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array((__, ___, ____) -> {
                    })
                    .cbor_parse_custom_item(PrimaryBlockItem::new, (__, ___, item) -> {
                        bundle = item.b;
                    })
                    .cbor_parse_array_items(CanonicalBlockItem::new, (__, ___, item) -> {
                        bundle.addBlock(item.block);
                    });
        }
    }

    static class PrimaryBlockItem extends BlockWithCRC {

        Bundle b;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .do_here((__) -> {
                        crc16 = CRC.init(CRC.CRCType.CRC16); // prepare CRC16 feeding
                        crc32 = CRC.init(CRC.CRCType.CRC32); // prepare CRC32 feeding
                    })
                    .do_for_each("crc-16", (__, buffer) -> crc16.read(buffer)) // feed CRC16 with every parsed buffer from this sequence
                    .do_for_each("crc-32", (__, buffer) -> crc32.read(buffer)) // feed CRC32 with every parsed buffer from this sequence
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
                                p.undo_for_each_now("crc-16");
                                p.undo_for_each_now("crc-32");
                                break;
                            case 1:
                                b.crcType = PrimaryBlock.CRCFieldType.CRC_16;
                                p.undo_for_each_now("crc-32");
                                crc = crc16Parser;
                                break;
                            case 2:
                                b.crcType = PrimaryBlock.CRCFieldType.CRC_32;
                                p.undo_for_each_now("crc-16");
                                crc = crc32Parser;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.destination = item.eid)
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.source = item.eid)
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> b.reportto = item.eid)
                    .cbor_open_array(2)
                    .cbor_parse_int((__, ___, i) -> b.creationTimestamp = i)
                    .cbor_parse_int((__, ___, i) -> {
                        b.sequenceNumber = i;
                        b.bid = new BundleID(b.source, b.creationTimestamp, b.sequenceNumber);
                    })
                    .cbor_parse_int((__, ___, i) -> b.lifetime = i)
                    .do_here(p -> p.insert_now(crc)) // validate crc
                    .do_here(__ -> b.crc_ok = this.crc_ok); // mark the block
        }
    }

    static class CanonicalBlockItem extends BlockWithCRC {

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
                            case PayloadBlock.type:
                                block = new PayloadBlock();
                                payload = blobBlock;
                                break;
                            case BlockIntegrityBlock.type:
                                block = new BlockIntegrityBlock();
                                payload = blockIntegrityBlock;
                                break;
                            case ManifestBlock.type:
                                block = new ManifestBlock();
                                payload = manifestBlock;
                                break;
                            case FlowLabelBlock.type:
                                block = new FlowLabelBlock();
                                payload = flowLabelBlock;
                                break;
                            case PreviousNodeBlock.type:
                                block = new PreviousNodeBlock();
                                payload = previousNodeBlock;
                                break;
                            case AgeBlock.type:
                                block = new AgeBlock();
                                payload = ageBlock;
                                break;
                            case ScopeControlHopLimitBlock.type:
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
                                p.undo_for_each_now("crc-16"); // deactivate CRC16 feeding
                                p.undo_for_each_now("crc-32"); // deactivate CRC32 feeding
                                break;
                            case 1:
                                block.crcType = BlockHeader.CRCFieldType.CRC_16;
                                p.undo_for_each_now("crc-32"); // deactivate CRC32 feeding
                                crc = crc16Parser;
                                break;
                            case 2:
                                block.crcType = BlockHeader.CRCFieldType.CRC_32;
                                p.undo_for_each_now("crc-16"); // deactivate CRC16 feeding
                                crc = crc32Parser;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .do_here(p -> p.insert_now(payload))
                    .do_here(p -> p.insert_now(crc))  // validate crc
                    .do_here(__ -> block.crc_ok = this.crc_ok); // mark the block
        }

        WritableBLOB wblob;
        CborParser blobBlock = CBOR.parser()
                .cbor_parse_byte_string(
                        (__, ___, size) -> {
                            try {
                                if (size >= 0) {
                                    ((BlockBLOB) block).data = BLOB.createBLOB((int) size);
                                } else {
                                    // indefinite length BLOB
                                    ((BlockBLOB) block).data = BLOB.createBLOB(2048); //todo change that
                                }
                            } catch (BundleStorage.StorageException sfe) {
                                ((BlockBLOB) block).data = new NullBLOB();
                            }
                            wblob = ((BlockBLOB) block).data.getWritableBLOB();
                        },
                        (__, chunk) -> {
                            if (wblob != null) {
                                try {
                                    wblob.write(chunk);
                                } catch (WritableBLOB.BLOBOverflowException io) {
                                    wblob.close();
                                    wblob = null;
                                } catch (IOException io) {
                                    wblob.close();
                                    wblob = null;
                                }
                            }
                        },
                        (__) -> {
                            if (wblob != null) {
                                wblob.close();
                            }
                        });

        CborParser blockIntegrityBlock = CBOR.parser(); //todo

        CborParser manifestBlock = CBOR.parser(); //todo

        CborParser flowLabelBlock = CBOR.parser(); //todo

        CborParser previousNodeBlock = CBOR.parser()
                .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> ((PreviousNodeBlock) block).previous = item.eid);

        CborParser ageBlock = CBOR.parser()
                .cbor_parse_int((p, __, i) -> ((AgeBlock) block).age = i)
                .do_here((p) -> ((AgeBlock) block).start());

        CborParser scopeControlLimitBlock = CBOR.parser()
                .cbor_open_array(2)
                .cbor_parse_int((p, __, i) -> ((ScopeControlHopLimitBlock) block).count = i)
                .cbor_parse_int((p, __, i) -> ((ScopeControlHopLimitBlock) block).limit = i);
    }

    abstract static class BlockWithCRC implements CborParser.ParseableItem {

        CborParser crc = CBOR.parser();

        CRC crc16;
        CRC crc32;
        boolean crc_ok = true;

        CborParser crc16Parser = CBOR.parser()
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
                        (__, buffer) -> {
                            crc_ok = crc16.doneAndValidate(buffer);
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
                            crc_ok = crc32.doneAndValidate(buffer);
                        });
    }

    static class EIDItem implements CborParser.ParseableItem {

        public EID eid;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_int((p, __, i) -> {
                        if (i == EID.IPN.EID_IPN_IANA_VALUE) { // IPN
                            p.insert_now(parseIPN);
                        } else { // DTN or UNKNOWN
                            p.insert_now(parseDTN);
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
