package io.left.rightmesh.libdtn.common.data.bundleV7.parser;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBLOB;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.eid.EIDFactory;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockDataParserFactory implements BlockDataParserFactory {

    @Override
    public CborParser create(int type,
                             CanonicalBlock block,
                             BLOBFactory blobFactory,
                             EIDFactory eidFactory,
                             Log logger) throws UnknownBlockTypeException {
        switch (type) {
            case PayloadBlock.type:
                return BlockBLOBParser.getParser((BlockBLOB) block, blobFactory, logger);
            case ManifestBlock.type:
                return ManifestBlockParser.getParser((ManifestBlock) block, logger);
            case FlowLabelBlock.type:
                return FlowLabelBlockParser.getParser((FlowLabelBlock) block, logger);
            case PreviousNodeBlock.type:
                return PreviousNodeBlockParser.getParser((PreviousNodeBlock) block, eidFactory, logger);
            case AgeBlock.type:
                return AgeBlockParser.getParser((AgeBlock) block, logger);
            case ScopeControlHopLimitBlock.type:
                return ScopeControlHopLimitBlockParser.getParser((ScopeControlHopLimitBlock) block, logger);
            case BlockAuthenticationBlock.type:
                return SecurityBlockParser.getParser((BlockAuthenticationBlock) block, eidFactory, logger);
            case BlockIntegrityBlock.type:
                return SecurityBlockParser.getParser((BlockIntegrityBlock) block, eidFactory, logger);
            case BlockConfidentialityBlock.type:
                return SecurityBlockParser.getParser((BlockConfidentialityBlock) block, eidFactory, logger);
            default:
                if(block instanceof UnknownExtensionBlock) {
                    return BlockBLOBParser.getParser((BlockBLOB) block, blobFactory, logger);
                } else {
                    throw new UnknownBlockTypeException();
                }
        }
    }

}
