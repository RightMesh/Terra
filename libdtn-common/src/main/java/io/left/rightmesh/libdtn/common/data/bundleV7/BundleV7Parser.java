package io.left.rightmesh.libdtn.common.data.bundleV7;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.CRC;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.data.eid.API;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.common.data.eid.DTN;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.IPN;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.NullBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.common.data.eid.UnkownEID;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Parser {

    private static final String TAG = "BundleV7Parser";

    public interface BundleParsedCallback {
        void onBundleParsed(Bundle b);
    }

    private Log logger;
    private BLOBFactory factory;

    public BundleV7Parser(Log logger, BLOBFactory factory) {
        this.factory = factory;
        this.logger = logger;
    }

    public CborParser createBundleParser(BundleParsedCallback cb) {
        return CBOR.parser().cbor_parse_custom_item(BundleItem::new, (__, ___, item) -> cb.onBundleParsed(item.bundle));
    }

    public BundleItem createBundleItem() {
        return new BundleItem();
    }

    public PrimaryBlockItem createPrimaryBlockItem() {
        return new PrimaryBlockItem();
    }

    public CanonicalBlockItem createCanonicalBlockItem() {
        return new CanonicalBlockItem();
    }

    public EIDItem createEIDItem() {
        return new EIDItem();
    }

    public class BundleItem implements CborParser.ParseableItem {

        public Bundle bundle = null;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array((__, ___, ____) -> {
                        logger.v(TAG, "[+] parsing new bundle");
                    })
                    .cbor_parse_custom_item(PrimaryBlockItem::new, (__, ___, item) -> {
                        logger.v(TAG, "-> primary block parsed");
                        bundle = item.b;
                    })
                    .cbor_parse_array_items(CanonicalBlockItem::new, (__, ___, item) -> {
                        logger.v(TAG, "-> canonical block parsed");
                        bundle.addBlock(item.block);
                    });
        }
    }

    public class PrimaryBlockItem extends BlockWithCRC {

        public Bundle b;

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
                                close_crc = crc16Parser;
                                break;
                            case 2:
                                b.crcType = PrimaryBlock.CRCFieldType.CRC_32;
                                p.undo_for_each_now("crc-16");
                                close_crc = crc32Parser;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
                        logger.v(TAG, ". destination=" + item.eid.getEIDString());
                        b.destination = item.eid;
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
                        logger.v(TAG, ". source=" + item.eid.getEIDString());
                        b.source = item.eid;
                    })
                    .cbor_parse_custom_item(EIDItem::new, (__, ___, item) -> {
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
                    .do_here(__ -> {
                        logger.v(TAG, ". crc_check=" + this.crc_ok);
                        b.tag("crc_check", this.crc_ok);
                    }); // tag the block
        }
    }

    public class CanonicalBlockItem extends BlockWithCRC {

        public CanonicalBlock block;
        CborParser payload;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .do_here((__) -> {
                        logger.v(TAG, ". preparing canonical block CRC");
                        crc16 = CRC.init(CRC.CRCType.CRC16); // prepare CRC16 feeding
                        crc32 = CRC.init(CRC.CRCType.CRC32); // prepare CRC32 feeding
                    })
                    .do_for_each("crc-16", (__, buffer) -> crc16.read(buffer))
                    .do_for_each("crc-32", (__, buffer) -> crc32.read(buffer))
                    .cbor_open_array((__, ___, i) -> {
                        logger.v(TAG, ". array size=" + i);
                        if ((i != 5) && (i != 6)) {
                            throw new RxParserException("wrong number of element in canonical block");
                        }
                    })
                    .cbor_parse_int((p, __, i) -> { // block type
                        logger.v(TAG, ". type=" + i);
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
                    .cbor_parse_int((__, ___, i) -> {
                        logger.v(TAG, ". number=" + i);
                        block.number = (int) i;
                    })
                    .cbor_parse_int((__, ___, i) -> {
                        logger.v(TAG, ". procV7flags=" + i);
                        block.procV7flags = i;
                    })
                    .cbor_parse_int((p, ___, i) -> {
                        logger.v(TAG, ". crc=" + i);
                        switch ((int) i) {
                            case 0:
                                block.crcType = BlockHeader.CRCFieldType.NO_CRC;
                                p.undo_for_each_now("crc-16"); // deactivate CRC16 feeding
                                p.undo_for_each_now("crc-32"); // deactivate CRC32 feeding
                                break;
                            case 1:
                                block.crcType = BlockHeader.CRCFieldType.CRC_16;
                                p.undo_for_each_now("crc-32"); // deactivate CRC32 feeding
                                close_crc = crc16Parser;
                                break;
                            case 2:
                                block.crcType = BlockHeader.CRCFieldType.CRC_32;
                                p.undo_for_each_now("crc-16"); // deactivate CRC16 feeding
                                close_crc = crc32Parser;
                                break;
                            default:
                                throw new RxParserException("wrong CRC type");
                        }
                    })
                    .do_here(p -> p.insert_now(payload))
                    .do_here(p -> p.insert_now(close_crc))  // validate close_crc
                    .do_here(__ -> {
                        logger.v(TAG, ". crc_check=" + this.crc_ok);
                        block.tag("crc_check", this.crc_ok);
                    }); // tag the block
        }

        WritableBLOB wblob;
        CborParser blobBlock = CBOR.parser()
                .cbor_parse_byte_string(
                        (__, ___, size) -> {
                            logger.v(TAG, ".. blob_byte_string_size="+size);
                            try {
                                ((BlockBLOB) block).data = factory.createBLOB((int) size);
                            } catch (BLOBFactory.BLOBFactoryException sfe) {
                                logger.v(TAG, ".. blob_create=NullBLOB");
                                ((BlockBLOB) block).data = new NullBLOB();
                            }
                            wblob = ((BlockBLOB) block).data.getWritableBLOB();
                        },
                        (__, chunk) -> {
                            logger.v(TAG, ".. blob_byte_chunk_size="+chunk.remaining());
                            if (wblob != null) {
                                try {
                                    wblob.write(chunk);
                                } catch (WritableBLOB.BLOBOverflowException | IOException io) {
                                    logger.v(TAG, ".. blob_write_error="+io.getMessage());
                                    wblob.close();
                                    wblob = null;
                                }
                            }
                        },
                        (__) -> {
                            logger.v(TAG, ".. blob_byte_string_finish");
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

    abstract class BlockWithCRC implements CborParser.ParseableItem {

        CborParser close_crc = CBOR.parser();

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

    public class EIDItem implements CborParser.ParseableItem {

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
                            } catch(EID.EIDFormatException efe) {
                                throw new RxParserException("API eid format exception: "+efe.getMessage());
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
}
