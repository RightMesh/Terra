package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;

/**
 * BaseBlockFactory implements the BlockFactory contract and can instantiate a new block
 * granted that the block type provided is one of the basic CanonicalBlock.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockFactory implements BlockFactory {

    @Override
    public CanonicalBlock create(int type) throws UnknownBlockTypeException {
        switch (type) {
            case PayloadBlock.type:
                return new PayloadBlock();
            case ManifestBlock.type:
                return new ManifestBlock();
            case FlowLabelBlock.type:
                return new FlowLabelBlock();
            case PreviousNodeBlock.type:
                return new PreviousNodeBlock();
            case AgeBlock.type:
                return new AgeBlock();
            case ScopeControlHopLimitBlock.type:
                return new ScopeControlHopLimitBlock();
            case BlockAuthenticationBlock.type:
                return new BlockAuthenticationBlock();
            case BlockIntegrityBlock.type:
                return new BlockIntegrityBlock();
            case BlockConfidentialityBlock.type:
                return new BlockConfidentialityBlock();
            default:
                throw new UnknownBlockTypeException();
        }
    }

}
