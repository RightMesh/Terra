package io.left.rightmesh.libdtn.common.data.bundleV7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;

/**
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockDataSerializerFactory implements BlockDataSerializerFactory {

    @Override
    public CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException {
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
                throw new UnknownBlockTypeException();
        }
    }
}
