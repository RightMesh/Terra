package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.CRC;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.reactivex.Flowable;

import static io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BundleV7Serializer.BUNDLE_VERSION_7;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class PrimaryBlockSerializer {

    /**
     * Serialize a {@link PrimaryBlock}
     *
     * @param block to serialize
     * @return Flowable
     */
    static CborEncoder encode(PrimaryBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(getItemCount(block))
                .cbor_encode_int(BUNDLE_VERSION_7)
                .cbor_encode_int(block.getProcV7Flags())
                .cbor_encode_int(block.getCrcType().ordinal())
                .merge(EIDSerializer.encode(block.getDestination()))
                .merge(EIDSerializer.encode(block.getSource()))
                .merge(EIDSerializer.encode(block.getReportto()))
                .cbor_start_array(2)
                .cbor_encode_int(block.getCreationTimestamp())
                .cbor_encode_int(block.getSequenceNumber())
                .cbor_encode_int(block.getLifetime());

        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            enc.cbor_encode_int(block.getFragmentOffset());
        }

        return enc.merge(encodeCRC(enc.observe(1000), block.getCrcType()));
    }

    static int getItemCount(PrimaryBlock block) {
        int length = 8;
        if (block.getCrcType() != PrimaryBlock.CRCFieldType.NO_CRC) {
            length++;
        }
        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            length++;
        }
        return length;
    }

    // encode PrimaryBlock CRC
    static CborEncoder encodeCRC(Flowable<ByteBuffer> source, PrimaryBlock.CRCFieldType type) {
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
