package io.left.rightmesh.libdtncommon.data.bundleV7;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtncommon.data.AgeBlock;
import io.left.rightmesh.libdtncommon.data.CanonicalBlock;
import io.left.rightmesh.libdtncommon.data.BlockBLOB;
import io.left.rightmesh.libdtncommon.data.BlockHeader;
import io.left.rightmesh.libdtncommon.data.BlockIntegrityBlock;
import io.left.rightmesh.libdtncommon.data.Bundle;
import io.left.rightmesh.libdtncommon.data.CRC;
import io.left.rightmesh.libdtncommon.data.eid.CLA;
import io.left.rightmesh.libdtncommon.data.eid.DTN;
import io.left.rightmesh.libdtncommon.data.eid.EID;
import io.left.rightmesh.libdtncommon.data.FlowLabelBlock;
import io.left.rightmesh.libdtncommon.data.ManifestBlock;
import io.left.rightmesh.libdtncommon.data.PayloadBlock;
import io.left.rightmesh.libdtncommon.data.PreviousNodeBlock;
import io.left.rightmesh.libdtncommon.data.PrimaryBlock;
import io.left.rightmesh.libdtncommon.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtncommon.data.eid.IPN;
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
    public static CborEncoder encode(Bundle bundle) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_indefinite_array()
                .merge(encode((PrimaryBlock) bundle));

        for (CanonicalBlock block : bundle.getBlocks()) {
            enc.merge(encode(block));
        }

        enc.cbor_stop_array();
        return enc;
    }

    /**
     * Serialize a {@see PrimaryBlock}
     *
     * @param block to serialize
     * @return Flowable
     */
    private static CborEncoder encode(PrimaryBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(getItemCount(block))
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

        return enc.merge(encodeCRC(enc.observe(1000), block.crcType));
    }

    private static CborEncoder encode(CanonicalBlock block) {

        CborEncoder enc = encode((BlockHeader) block);

        switch (block.type) {
            case PayloadBlock.type:
                enc.merge(encode((BlockBLOB) block));
                break;
            case BlockIntegrityBlock.type:
                enc.merge(encode((BlockIntegrityBlock) block));
                break;
            case ManifestBlock.type:
                enc.merge(encode((ManifestBlock) block));
                break;
            case FlowLabelBlock.type:
                enc.merge(encode((FlowLabelBlock) block));
                break;
            case PreviousNodeBlock.type:
                enc.merge(encode((PreviousNodeBlock) block));
                break;
            case AgeBlock.type:
                enc.merge(encode((AgeBlock) block));
                break;
            case ScopeControlHopLimitBlock.type:
                enc.merge(encode((ScopeControlHopLimitBlock) block));
                break;
            default:
                break;
        }
        return enc.merge(encodeCRC(enc.observe(50), block.crcType));
    }

    private static CborEncoder encode(BlockHeader block) {
        return CBOR.encoder()
                .cbor_start_array(getItemCount(block))
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
        return encode(block.previous);
    }

    private static CborEncoder encode(AgeBlock block) {
        block.stop();
        long age = block.age + block.time_end - block.time_start;
        return CBOR.encoder()
                .cbor_encode_int(age);
    }

    private static CborEncoder encode(ScopeControlHopLimitBlock block) {
        return CBOR.encoder()
                .cbor_start_array(2)
                .cbor_encode_int(block.count)
                .cbor_encode_int(block.limit);
    }

    private static CborEncoder encode(EID eid) {
        if (eid.equals(DTN.NullEID())) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_encode_int(0);
        }
        if (eid instanceof IPN) {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_start_array(2)
                    .cbor_encode_int(((IPN) eid).node_number)
                    .cbor_encode_int(((IPN) eid).service_number);
        } else {
            return CBOR.encoder()
                    .cbor_start_array(2)
                    .cbor_encode_int(eid.IANA())
                    .cbor_encode_text_string(eid.getSsp());
        }
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

    private static int getItemCount(PrimaryBlock block) {
        int length = 8;
        if (block.crcType != PrimaryBlock.CRCFieldType.NO_CRC) {
            length++;
        }
        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            length++;
        }
        return length;
    }

    private static int getItemCount(BlockHeader block) {
        int length = 5; // 6 in draft-BPbis v10, currently v11
        if (block.crcType != BlockHeader.CRCFieldType.NO_CRC) {
            length++;
        }
        return length;
    }
}
