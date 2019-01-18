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

        if (block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
            enc.merge(BlockBlobSerializer.encode((BlockBlob) block));
        } else {
            try {
                enc.merge(factory.create(block));
            } catch (BlockDataSerializerFactory.UnknownBlockTypeException ubte) {
                enc.merge(CBOR.encoder());
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
