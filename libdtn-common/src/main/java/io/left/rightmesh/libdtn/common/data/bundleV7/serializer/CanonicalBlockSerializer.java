package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.common.data.security.EncryptedBlock;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 04/11/18.
 */
public class CanonicalBlockSerializer {

    public static CborEncoder encode(CanonicalBlock block) {
        CborEncoder enc = BlockHeaderSerializer.encode(block)
                .merge(encodePayload(block));

        return enc.merge(encodeCRC(enc.observe(50), block.crcType));
    }

    public static CborEncoder encodePayload(CanonicalBlock block) {

        if(block.getV7Flag(BlockHeader.BlockV7Flags.BLOCK_IS_ENCRYPTED)) {
            return BlockBLOBSerializer.encode((BlockBLOB) block);
        }

        switch (block.type) {
            case PayloadBlock.type:
                return BlockBLOBSerializer.encode((BlockBLOB) block);
            case ManifestBlock.type:
                return ManifestBlockSerializer.encode((ManifestBlock) block);
            case FlowLabelBlock.type:
                return FlowLabelBlockSerializer.encode((FlowLabelBlock) block);
            case PreviousNodeBlock.type:
                return PreviousNodeBlockSerializer.encode((PreviousNodeBlock) block);
            case AgeBlock.type:
                return AgeBlockSerializer.encode((AgeBlock) block);
            case ScopeControlHopLimitBlock.type:
                return ScopeControlHopLimitBlockSerializer.encode((ScopeControlHopLimitBlock) block);
            case BlockConfidentialityBlock.type:
            case BlockIntegrityBlock.type:
            case BlockAuthenticationBlock.type:
                return SecurityBlockSerializer.encode((AbstractSecurityBlock)block);
            default:
                return CBOR.encoder();
        }
    }

    // encode BLOCK CRC
    private static CborEncoder encodeCRC(Flowable<ByteBuffer> source, BlockHeader.CRCFieldType type) {
        if (type == BlockHeader.CRCFieldType.CRC_16) {
            return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_16);
        }
        if (type == BlockHeader.CRCFieldType.CRC_32) {
            return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.CRC_32);
        }
        return PrimaryBlockSerializer.encodeCRC(source, PrimaryBlock.CRCFieldType.NO_CRC);
    }
}
