package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.reactivex.Flowable;

import java.nio.ByteBuffer;

/**
 * CanonicalBlockSerializer serializes a {@link CanonicalBlock}.
 *
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockSerializer {

    /**
     * serializes a {@link CanonicalBlock}.
     *
     * @param block to serialize.
     * @param factory to create a block-specific data serializer
     * @return a Cbor-encoded serialized CanonicalBlock.
     */
    public static CborEncoder encode(CanonicalBlock block, BlockDataSerializerFactory factory) {
        CborEncoder enc = BlockHeaderSerializer.encode(block);

        if (block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)
                || block instanceof BlockBlob) {
            enc.cbor_encode_byte_string(
                    ((BlockBlob) block).data.size(),
                    ((BlockBlob) block).data.observe());
        } else {
            try {
                enc.cbor_encode_byte_string(factory.create(block).observe(), true);
            } catch (BlockDataSerializerFactory.UnknownBlockTypeException ubte) {
                // that should never happen. It basically means that we added an extension block
                // with no serializer which should be forbidden.
                enc.cbor_encode_byte_string(new byte[]{});
            }
        }

        return enc.merge(encodeCrc(enc.observe(50), block.crcType));
    }


    static CborEncoder encodeCrc(Flowable<ByteBuffer> source,
                                 BlockHeader.CrcFieldType type) {
        if (type == BlockHeader.CrcFieldType.CRC_16) {
            return PrimaryBlockSerializer.encodeCrc(source, PrimaryBlock.CrcFieldType.CRC_16);
        }
        if (type == BlockHeader.CrcFieldType.CRC_32) {
            return PrimaryBlockSerializer.encodeCrc(source, PrimaryBlock.CrcFieldType.CRC_32);
        }
        return PrimaryBlockSerializer.encodeCrc(source, PrimaryBlock.CrcFieldType.NO_CRC);
    }
}
