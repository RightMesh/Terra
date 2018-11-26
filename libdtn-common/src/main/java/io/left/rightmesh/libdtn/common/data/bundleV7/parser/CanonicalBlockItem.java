package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.BaseBlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CRC;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockItem implements CborParser.ParseableItem {

    public CanonicalBlockItem(Log logger, BLOBFactory blobFactory) {
        this.logger = logger;
        this.blockFactory = new BaseBlockFactory();
        this.parserFactory = new BaseBlockDataParserFactory();
        this.blobFactory = blobFactory;
    }

    public CanonicalBlockItem(Log logger,
                              BlockFactory blockFactory,
                              BlockDataParserFactory parserFactory,
                              BLOBFactory blobFactory) {
        this.logger = logger;
        this.blockFactory = blockFactory;
        this.parserFactory = parserFactory;
        this.blobFactory = blobFactory;
    }

    public CanonicalBlock block;

    private Log logger;
    private BlockFactory blockFactory;
    private BlockDataParserFactory parserFactory;
    private BLOBFactory blobFactory;
    private CborParser payloadParser;
    private CRC crc16;
    private CRC crc32;
    private boolean crc_ok = true;
    private CborParser close_crc = CBOR.parser();

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .do_here((p) -> {
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
                    try {
                        block = blockFactory.create((int) i);
                    } catch(BlockFactory.UnknownBlockTypeException ubte) {
                        block = new UnknownExtensionBlock((int) i);
                    }

                    try {
                        payloadParser = parserFactory.create((int) i, block, blobFactory, logger);
                    } catch(BlockDataParserFactory.UnknownBlockTypeException ubte) {
                        payloadParser = BlockBLOBParser.getParser((BlockBLOB) block, blobFactory, logger);
                    }
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". number=" + i);
                    block.number = (int) i;
                })
                .cbor_parse_int((__, ___, i) -> {
                    logger.v(TAG, ". procV7flags=" + i);
                    block.procV7flags = i;
                    if (block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
                        payloadParser = BlockBLOBParser.getParser((BlockBLOB) block, blobFactory, logger);
                    }
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
                            close_crc = crc16CloseParser();
                            break;
                        case 2:
                            block.crcType = BlockHeader.CRCFieldType.CRC_32;
                            p.undo_for_each_now("crc-16"); // deactivate CRC16 feeding
                            close_crc = crc32CloseParser();
                            break;
                        default:
                            throw new RxParserException("wrong CRC type");
                    }
                })
                .do_here(p -> p.insert_now(payloadParser))
                .do_here(p -> p.insert_now(close_crc))  // validate close_crc
                .do_here(p -> {
                    logger.v(TAG, ". crc_check=" + crc_ok);
                    block.tag("crc_check", crc_ok);
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
