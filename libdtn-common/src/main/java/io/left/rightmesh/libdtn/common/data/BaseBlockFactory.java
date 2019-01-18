package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;

/**
 * BaseBlockFactory implements the BlockFactory contract and can instantiate a new block
 * granted that the block PAYLOAD_BLOCK_TYPE provided is one of the basic CanonicalBlock.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockFactory implements BlockFactory {

    @Override
    public CanonicalBlock create(int type) throws UnknownBlockTypeException {
        switch (type) {
            case PayloadBlock.PAYLOAD_BLOCK_TYPE:
                return new PayloadBlock();
            case ManifestBlock.MANIFEST_BLOCK_TYPE:
                return new ManifestBlock();
            case FlowLabelBlock.FLOW_LABEL_BLOCK_TYPE:
                return new FlowLabelBlock();
            case PreviousNodeBlock.PREVIOUS_NODE_BLOCK_TYPE:
                return new PreviousNodeBlock();
            case AgeBlock.AGE_BLOCK_TYPE:
                return new AgeBlock();
            case ScopeControlHopLimitBlock.SCOPE_CONTROL_HOP_LIMIT_BLOCK_TYPE:
                return new ScopeControlHopLimitBlock();
            case BlockAuthenticationBlock.BLOCK_AUTHENTICATION_BLOCK_TYPE:
                return new BlockAuthenticationBlock();
            case BlockIntegrityBlock.BLOCK_INTEGRITY_BLOCK_TYPE:
                return new BlockIntegrityBlock();
            case BlockConfidentialityBlock.BLOCK_CONFIDENTIALITY_BLOCK_TYPE:
                return new BlockConfidentialityBlock();
            default:
                throw new UnknownBlockTypeException();
        }
    }

}
