package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import static io.left.rightmesh.libdtn.common.data.bundlev7.parser.BundleV7Item.TAG;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.parser.RxParserException;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.Crc;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.NullBlob;
import io.left.rightmesh.libdtn.common.data.blob.WritableBlob;
import io.left.rightmesh.libdtn.common.utils.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * CanonicalBlockItem is a ParseableItem for a {@link CanonicalBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockItem implements ParseableItem {

    /**
     * A BundleItem requires a toolbox to be able to parse extension Block and
     * extension Eid. It also need a BlobFactory to create a new Blob to hold the payload.
     *
     * @param logger      to output parsing information
     * @param toolbox     for the data structure factory
     * @param blobFactory to create blobs.
     */
    public CanonicalBlockItem(Log logger,
                              ExtensionToolbox toolbox,
                              BlobFactory blobFactory) {
        this.logger = logger;
        this.toolbox = toolbox;
        this.blobFactory = blobFactory;
    }

    public CanonicalBlock block;

    private Log logger;
    private ExtensionToolbox toolbox;
    private BlobFactory blobFactory;
    private CborParser payloadParser;
    private Crc crc16;
    private Crc crc32;
    private boolean crcOk = true;
    private CborParser closeCrc = CBOR.parser();

    @Override
    public CborParser getItemParser() {
        return CBOR.parser()
                .do_here((p) -> {
                    logger.v(TAG, ". preparing canonical block Crc");
                    crc16 = Crc.init(Crc.CrcType.CRC16); // prepare Crc16 feeding
                    crc32 = Crc.init(Crc.CrcType.CRC32); // prepare Crc32 feeding
                })
                .do_for_each("crc-16", (p, buffer) -> crc16.read(buffer))
                .do_for_each("crc-32", (p, buffer) -> crc32.read(buffer))
                .cbor_open_array((parser, tags, i) -> {
                    logger.v(TAG, ". array size=" + i);
                    if ((i != 5) && (i != 6)) {
                        throw new RxParserException("wrong number of element in canonical block");
                    }
                })
                .cbor_parse_int((p, t, i) -> { // block type
                    logger.v(TAG, ". type=" + i);
                    try {
                        block = toolbox.getBlockFactory().create((int) i);
                    } catch (BlockFactory.UnknownBlockTypeException ubte) {
                        block = new UnknownExtensionBlock((int) i);
                    }

                    try {
                        payloadParser = toolbox.getBlockDataParserFactory().create(
                                (int) i,
                                block,
                                blobFactory,
                                toolbox.getEidFactory(), logger);
                    } catch (BlockDataParserFactory.UnknownBlockTypeException
                            | BlockDataParserFactory.UnstructuredPayloadException ubte) {
                        payloadParser = null;
                    }
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". number=" + i);
                    block.number = (int) i;
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". procV7flags=" + i);
                    block.procV7flags = i;
                    if (block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
                        payloadParser = null;
                    }
                })
                .cbor_parse_int((p, t, i) -> {
                    logger.v(TAG, ". crc=" + i);
                    switch ((int) i) {
                        case 0:
                            block.crcType = BlockHeader.CrcFieldType.NO_CRC;
                            p.undo_for_each_now("crc-16"); // deactivate Crc16 feeding
                            p.undo_for_each_now("crc-32"); // deactivate Crc32 feeding
                            break;
                        case 1:
                            block.crcType = BlockHeader.CrcFieldType.CRC_16;
                            p.undo_for_each_now("crc-32"); // deactivate Crc32 feeding
                            closeCrc = crc16CloseParser();
                            break;
                        case 2:
                            block.crcType = BlockHeader.CrcFieldType.CRC_32;
                            p.undo_for_each_now("crc-16"); // deactivate Crc16 feeding
                            closeCrc = crc32CloseParser();
                            break;
                        default:
                            throw new RxParserException("wrong Crc type");
                    }
                })
                .cbor_parse_byte_string(
                        (parser, tag, size) -> {
                            if (size == -1) {
                                // since bpbis-13
                                throw new RxParserException("the block payload must be of "
                                        + "a definite length.");
                            } else {
                                logger.v(TAG, ". payload size=" + size);
                                block.dataSize = size;
                                if (payloadParser == null) {
                                    try {
                                        ((BlockBlob) block).data = blobFactory
                                                .createBlob((int) size);
                                    } catch (BlobFactory.BlobFactoryException sfe) {
                                        logger.v(TAG, ".. payload blob create=NullBlob");
                                        ((BlockBlob) block).data = new NullBlob();
                                    }
                                    parser.setReg(3, ((BlockBlob) block).data.getWritableBlob());
                                }
                            }
                        },
                        (parser, byteChunk) -> {
                            if (payloadParser == null) {
                                logger.v(TAG, ".. payload chunk size=" + byteChunk.remaining());
                                logger.v(TAG, ".. payload chunk=" + new String(byteChunk.array()));
                                try {
                                    parser.<WritableBlob>getReg(3).write(byteChunk);
                                } catch (WritableBlob.BlobOverflowException | IOException io) {
                                    logger.v(TAG, ".. payload blob write error=" + io.getMessage());
                                    parser.<WritableBlob>getReg(3).close();
                                    parser.setReg(3, null);
                                }
                            } else {
                                while (byteChunk.hasRemaining()) {
                                    payloadParser.read(byteChunk);
                                }
                            }
                        },
                        (parser) -> {
                            if (payloadParser == null) {
                                if (parser.getReg(3) != null) {
                                    parser.<WritableBlob>getReg(3).close();
                                }
                                logger.v(TAG, ".. payload blob closed");
                            } else {
                                if (!payloadParser.isDone()) {
                                    throw new RxParserException("the block payload is missing data");
                                } else {
                                    logger.v(TAG, ".. payload data parsed");
                                }
                            }
                        })
                .do_here(p -> p.insert_now(closeCrc))  // validate closeCrc
                .do_here(p -> {
                    logger.v(TAG, ". crc_check=" + crcOk);
                    block.tag("crc_check", crcOk);
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
