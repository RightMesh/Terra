package io.left.rightmesh.libdtn.data.bundleV7;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.data.AgeBlock;
import io.left.rightmesh.libdtn.data.Block;
import io.left.rightmesh.libdtn.data.BlockBLOB;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.CRC;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.data.ManifestBlock;
import io.left.rightmesh.libdtn.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.data.ScopeControlHopLimitBlock;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public class BundleV7Serializer {

    public static final byte BUNDLE_VERSION_7 = 0x07;

    /**
     * Turn a Bundle into a serialized Flowable of ByteBuffer.
     *
     * @param bundle to serialize
     * @return Flowable
     */
    public static Flowable<ByteBuffer> serialize(Bundle bundle) {
        CborEncoder enc = encode((PrimaryBlock) bundle);
        for (Block block : bundle.getBlocks()) {
            enc.merge(encode(block));
        }
        return enc.encode();
    }

    /**
     * Serialize a {@see PrimaryBlock}
     *
     * @param block to serialize
     * @return Flowable
     */
    private static CborEncoder encode(PrimaryBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_encode_int(BUNDLE_VERSION_7)
                .cbor_encode_int(block.procV7Flags)
                .cbor_encode_int(block.crcType.ordinal())
                .merge(encode(block.destination))
                .merge(encode(block.source))
                .merge(encode(block.reportto))
                .cbor_start_array(2)
                .cbor_encode_int(block.creationTimestamp)
                .cbor_encode_int(block.sequenceNumber)
                .cbor_encode_int(block.lifetime);

        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            enc.cbor_encode_int(block.fragmentOffset);
        }

        return enc.merge(encodeCRC(enc.encode(), block.crcType));
    }

    private static CborEncoder encode(Block block) {

        CborEncoder enc = encode((BlockHeader)block);

        switch (block.type) {
            case 0:
                enc.merge(encode((BlockBLOB) block));
                break;
            case 2:
                enc.merge(encode((BlockIntegrityBlock) block));
                break;
            case 4:
                enc.merge(encode((ManifestBlock) block));
                break;
            case 6:
                enc.merge(encode((FlowLabelBlock) block));
                break;
            case 7:
                enc.merge(encode((PreviousNodeBlock) block));
                break;
            case 8:
                enc.merge(encode((AgeBlock) block));
                break;
            case 9:
                enc.merge(encode((ScopeControlHopLimitBlock) block));
                break;
            default:
                break;
        }
        return enc.merge(encodeCRC(enc.encode(), block.crcType));
    }

    private static CborEncoder encode(BlockHeader block) {
        return CBOR.encoder()
                .cbor_start_array(block.crcType == BlockHeader.CRCFieldType.NO_CRC ? 5 : 6)
                .cbor_encode_int(block.type)
                .cbor_encode_int(block.number)
                .cbor_encode_int(block.procV7flags)
                .cbor_encode_int(block.crcType.ordinal());
    }

    private static CborEncoder encode(BlockBLOB block) {
        return CBOR.encoder()
                .cbor_encode_byte_string(block.data.observe());
    }

    private static CborEncoder encode(BlockIntegrityBlock block) {
        return CBOR.encoder();
    }

    private static CborEncoder encode(ManifestBlock block) {
        return CBOR.encoder();
    }

    private static CborEncoder encode(FlowLabelBlock block) {
        return CBOR.encoder();
    }

    private static CborEncoder encode(PreviousNodeBlock block) {
        return CBOR.encoder();
    }

    private static CborEncoder encode(AgeBlock block) {
        return CBOR.encoder()
                .cbor_encode_int(block.age);
    }

    private static CborEncoder encode(ScopeControlHopLimitBlock block) {
        return CBOR.encoder()
                .cbor_encode_int(block.count)
                .cbor_encode_int(block.limit);
    }

    private static CborEncoder encode(EID eid) {
        if (eid.equals(EID.NullEID())) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_encode_int(0);
        }

        if (eid instanceof EID.DTN) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_encode_text_string(eid.getSsp());
        }

        if (eid instanceof EID.IPN) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_start_array(2)
                    .cbor_encode_int(((EID.IPN) eid).node_number)
                    .cbor_encode_int(((EID.IPN) eid).service_number);
        }
        return CBOR.encoder(); // that should not happen
    }



    // encode BLOCK CRC
    private static CborEncoder encodeCRC(Flowable<ByteBuffer> source, BlockHeader.CRCFieldType type) {
        if (type == BlockHeader.CRCFieldType.CRC_16) {
            return encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_16);
        }
        if (type == BlockHeader.CRCFieldType.CRC_32) {
            return encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_32);
        }
        return encodeCRC(source, PrimaryBlock.CRCFieldType.NO_CRC);
    }

    // encode PrimaryBlock CRC
    private static CborEncoder encodeCRC(Flowable<ByteBuffer> source, PrimaryBlock.CRCFieldType type) {
        if (type == PrimaryBlock.CRCFieldType.CRC_16) {
            byte[] zeroCRC = {0x42, 0x00, 0x00};
            Flowable<ByteBuffer> crcStream = source.concatWith(Flowable.just(ByteBuffer.wrap(zeroCRC)));
            return CBOR.encoder().cbor_encode_byte_string(CRC.compute_crc16(crcStream));
        }

        if (type == PrimaryBlock.CRCFieldType.CRC_32) {
            byte[] zeroCRC = {0x44, 0x00, 0x00, 0x00, 0x00};
            Flowable<ByteBuffer> crcStream = source.concatWith(Flowable.just(ByteBuffer.wrap(zeroCRC)));
            return CBOR.encoder().cbor_encode_byte_string(CRC.compute_crc32(crcStream));
        }

        return CBOR.encoder();
    }

}
