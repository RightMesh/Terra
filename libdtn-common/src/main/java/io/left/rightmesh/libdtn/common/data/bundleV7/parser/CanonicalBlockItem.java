package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CRC;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

import static io.left.rightmesh.libdtn.common.data.bundleV7.parser.BundleV7Item.TAG;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockItem implements CborParser.ParseableItem {

    public CanonicalBlockItem(Log logger, BLOBFactory factory) {
        this.logger = logger;
        this.factory = factory;
    }

    public CanonicalBlock block;

    private Log logger;
    private BLOBFactory factory;
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
                    switch ((int) i) {
                        case PayloadBlock.type:
                            block = new PayloadBlock();
                            payloadParser = BlockBLOBParser.getParser((BlockBLOB)block, factory, logger);
                            break;
                        case ManifestBlock.type:
                            block = new ManifestBlock();
                            payloadParser = ManifestBlockParser.getParser((ManifestBlock)block, logger);
                            break;
                        case FlowLabelBlock.type:
                            block = new FlowLabelBlock();
                            payloadParser = FlowLabelBlockParser.getParser((FlowLabelBlock)block, logger);
                            break;
                        case PreviousNodeBlock.type:
                            block = new PreviousNodeBlock();
                            payloadParser = PreviousNodeBlockParser.getParser((PreviousNodeBlock)block, logger);
                            break;
                        case AgeBlock.type:
                            block = new AgeBlock();
                            payloadParser = AgeBlockParser.getParser((AgeBlock)block, logger);
                            break;
                        case ScopeControlHopLimitBlock.type:
                            block = new ScopeControlHopLimitBlock();
                            payloadParser = ScopeControlHopLimitBlockParser.getParser((ScopeControlHopLimitBlock)block, logger);
                            break;
                        case BlockAuthenticationBlock.type:
                            block = new BlockAuthenticationBlock();
                            payloadParser = SecurityBlockParser.getParser((BlockAuthenticationBlock) block, logger);
                            break;
                        case BlockIntegrityBlock.type:
                            block = new BlockIntegrityBlock();
                            payloadParser = SecurityBlockParser.getParser((BlockIntegrityBlock) block, logger);
                            break;
                        case BlockConfidentialityBlock.type:
                            block = new BlockConfidentialityBlock();
                            payloadParser = SecurityBlockParser.getParser((BlockConfidentialityBlock) block, logger);
                            break;
                        default:
                            block = new UnknownExtensionBlock((int) i);
                            payloadParser = BlockBLOBParser.getParser((BlockBLOB)block, factory, logger);
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
                    if(block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
                        payloadParser = BlockBLOBParser.getParser((BlockBLOB)block, factory, logger);
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
