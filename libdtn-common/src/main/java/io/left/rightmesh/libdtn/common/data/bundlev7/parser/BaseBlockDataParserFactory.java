package io.left.rightmesh.libdtn.common.data.bundlev7.parser;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.BlockBlob;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.data.UnknownExtensionBlock;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.eid.EidFactory;
import io.left.rightmesh.libdtn.common.data.security.BlockAuthenticationBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockConfidentialityBlock;
import io.left.rightmesh.libdtn.common.data.security.BlockIntegrityBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * BaseBlockDataParserFactory implements BlockDataParserFactory
 * and provides parser for all the basic blocks.
 *
 * @author Lucien Loiseau on 21/11/18.
 */
public class BaseBlockDataParserFactory implements BlockDataParserFactory {

    @Override
    public CborParser create(int type,
                             CanonicalBlock block,
                             BlobFactory blobFactory,
                             EidFactory eidFactory,
                             Log logger) throws UnknownBlockTypeException {
        switch (type) {
            case PayloadBlock.PAYLOAD_BLOCK_TYPE:
                return BlockBlobParser
                        .getParser((BlockBlob) block, blobFactory, logger);
            case ManifestBlock.MANIFEST_BLOCK_TYPE:
                return ManifestBlockParser
                        .getParser((ManifestBlock) block, logger);
            case FlowLabelBlock.FLOW_LABEL_BLOCK_TYPE:
                return FlowLabelBlockParser
                        .getParser((FlowLabelBlock) block, logger);
            case PreviousNodeBlock.PREVIOUS_NODE_BLOCK_TYPE:
                return PreviousNodeBlockParser
                        .getParser((PreviousNodeBlock) block, eidFactory, logger);
            case AgeBlock.AGE_BLOCK_TYPE:
                return AgeBlockParser
                        .getParser((AgeBlock) block, logger);
            case ScopeControlHopLimitBlock.SCOPE_CONTROL_HOP_LIMIT_BLOCK_TYPE:
                return ScopeControlHopLimitBlockParser
                        .getParser((ScopeControlHopLimitBlock) block, logger);
            case BlockAuthenticationBlock.BLOCK_AUTHENTICATION_BLOCK_TYPE:
                return SecurityBlockParser
                        .getParser((BlockAuthenticationBlock) block, eidFactory, logger);
            case BlockIntegrityBlock.BLOCK_INTEGRITY_BLOCK_TYPE:
                return SecurityBlockParser
                        .getParser((BlockIntegrityBlock) block, eidFactory, logger);
            case BlockConfidentialityBlock.BLOCK_CONFIDENTIALITY_BLOCK_TYPE:
                return SecurityBlockParser
                        .getParser((BlockConfidentialityBlock) block, eidFactory, logger);
            default:
                if (block instanceof UnknownExtensionBlock) {
                    return BlockBlobParser
                            .getParser((BlockBlob) block, blobFactory, logger);
                } else {
                    throw new UnknownBlockTypeException();
                }
        }
    }

}
