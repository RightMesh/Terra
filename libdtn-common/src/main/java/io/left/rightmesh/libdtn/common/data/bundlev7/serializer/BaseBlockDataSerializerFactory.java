package io.left.rightmesh.libdtn.common.data.bundlev7.serializer;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.RoutingBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.security.AbstractSecurityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;

/**
 * BaseBlockDataSerializerFactory implements {@link BlockDataSerializerFactory} for all the
 * basic blocks.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockDataSerializerFactory implements BlockDataSerializerFactory {

    @Override
    public CborEncoder create(CanonicalBlock block) throws UnknownBlockTypeException {
        switch (block.type) {
            case PayloadBlock.PAYLOAD_BLOCK_TYPE:
                return BlockBlobSerializer
                        .encode((BlockBlob) block);
            case RoutingBlock.ROUTING_BLOCK_TYPE:
                return RoutingBlockSerializer
                        .encode((RoutingBlock) block);
            case ManifestBlock.MANIFEST_BLOCK_TYPE:
                return ManifestBlockSerializer
                        .encode((ManifestBlock) block);
            case FlowLabelBlock.FLOW_LABEL_BLOCK_TYPE:
                return FlowLabelBlockSerializer
                        .encode((FlowLabelBlock) block);
            case PreviousNodeBlock.PREVIOUS_NODE_BLOCK_TYPE:
                return PreviousNodeBlockSerializer
                        .encode((PreviousNodeBlock) block);
            case AgeBlock.AGE_BLOCK_TYPE:
                return AgeBlockSerializer
                        .encode((AgeBlock) block);
            case ScopeControlHopLimitBlock.SCOPE_CONTROL_HOP_LIMIT_BLOCK_TYPE:
                return ScopeControlHopLimitBlockSerializer
                        .encode((ScopeControlHopLimitBlock) block);
            case BlockConfidentialityBlock.BLOCK_CONFIDENTIALITY_BLOCK_TYPE:
            case BlockIntegrityBlock.BLOCK_INTEGRITY_BLOCK_TYPE:
            case BlockAuthenticationBlock.BLOCK_AUTHENTICATION_BLOCK_TYPE:
                return SecurityBlockSerializer
                        .encode((AbstractSecurityBlock)block);
            default:
                throw new UnknownBlockTypeException();
        }
    }
}
