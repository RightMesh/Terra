package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import static io.left.rightmesh.libdtn.common.data.bundlev7.serializer.BundleV7Serializer.BUNDLE_VERSION_7;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.Crc;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * PrimaryBlockSerializer serializes a {@link PrimaryBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class PrimaryBlockSerializer {

    /**
     * Serialize a {@link PrimaryBlock}.
     *
     * @param block to serialize
     * @return a Cbor-encoded serialized PrimaryBlock.
     */
    static CborEncoder encode(PrimaryBlock block) {
        CborEncoder enc = CBOR.encoder()
                .cbor_start_array(getItemCount(block))
                .cbor_encode_int(BUNDLE_VERSION_7)
                .cbor_encode_int(block.getProcV7Flags())
                .cbor_encode_int(block.getCrcType().ordinal())
                .merge(EidSerializer.encode(block.getDestination()))
                .merge(EidSerializer.encode(block.getSource()))
                .merge(EidSerializer.encode(block.getReportto()))
                .cbor_start_array(2)
                .cbor_encode_int(block.getCreationTimestamp())
                .cbor_encode_int(block.getSequenceNumber())
                .cbor_encode_int(block.getLifetime());

        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            enc.cbor_encode_int(block.getFragmentOffset());
        }

        return enc.merge(encodeCrc(enc.observe(1000), block.getCrcType()));
    }

    static int getItemCount(PrimaryBlock block) {
        int length = 8;
        if (block.getCrcType() != PrimaryBlock.CrcFieldType.NO_CRC) {
            length++;
        }
        if (block.getV7Flag(PrimaryBlock.BundleV7Flags.FRAGMENT)) {
            length++;
        }
        return length;
    }

    // encode PrimaryBlock Crc
    static CborEncoder encodeCrc(Flowable<ByteBuffer> source, PrimaryBlock.CrcFieldType type) {
        if (type == PrimaryBlock.CrcFieldType.CRC_16) {
            byte[] zeroCrc = {0x42, 0x00, 0x00};
            Flowable<ByteBuffer> crcStream = source
                    .concatWith(Flowable.just(ByteBuffer.wrap(zeroCrc)));
            return CBOR.encoder().cbor_encode_byte_string(Crc.compute_crc16(crcStream));
        }

        if (type == PrimaryBlock.CrcFieldType.CRC_32) {
            byte[] zeroCrc = {0x44, 0x00, 0x00, 0x00, 0x00};
            Flowable<ByteBuffer> crcStream = source
                    .concatWith(Flowable.just(ByteBuffer.wrap(zeroCrc)));
            return CBOR.encoder().cbor_encode_byte_string(Crc.compute_crc32(crcStream));
        }

        return CBOR.encoder();
    }
}
