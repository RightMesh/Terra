package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockSerializer {

    public static CborEncoder encode(CanonicalBlock block, BlockDataSerializerFactory factory) {
        CborEncoder enc = BlockHeaderSerializer.encode(block);

        if(block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
            enc.merge(BlockBLOBSerializer.encode((BlockBLOB) block));
        } else {
            try {
                enc.merge(factory.create(block));
            } catch(BlockDataSerializerFactory.UnknownBlockTypeException ubte) {
                enc.merge(CBOR.encoder());
            }
        }

        return enc.merge(encodeCRC(enc.observe(50), block.crcType));
    }

    public static CborEncoder encodeCRC(Flowable<ByteBuffer> source, BlockHeader.CRCFieldType type) {
        if (type == BlockHeader.CRCFieldType.CRC_16) {
            return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_16);
        }
        if (type == BlockHeader.CRCFieldType.CRC_32) {
            return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_32);
        }
        return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.NO_CRC);
    }
}
