package io.left.rightmesh.libdtn.common.data.bundlev7.processor;

import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.RoutingBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

import java.util.Arrays;

/**
 * BaseBlockProcessorFactory implements BlockProcessorFactory for all the basic blocks.
 *
 * @author Lucien Loiseau on 26/11/18.
 */
public class BaseBlockProcessorFactory implements BlockProcessorFactory {

    static BlockProcessor nullProcessor = new BlockProcessor() {
        @Override
        public void onBlockDeserialized(CanonicalBlock block) throws ProcessingException {
        }

        @Override
        public boolean onReceptionProcessing(CanonicalBlock block, Bundle bundle, Log logger)
                throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPutOnStorage(CanonicalBlock block, Bundle bundle, Log logger)
                throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPullFromStorage(CanonicalBlock block, Bundle bundle, Log logger)
                throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPrepareForTransmission(CanonicalBlock block, Bundle bundle, Log logger)
                throws ProcessingException {
            return false;
        }
    };

    static Integer[] basicBlockTypes = {
            PayloadBlock.PAYLOAD_BLOCK_TYPE,
            RoutingBlock.ROUTING_BLOCK_TYPE,
            ManifestBlock.MANIFEST_BLOCK_TYPE,
            FlowLabelBlock.FLOW_LABEL_BLOCK_TYPE,
            PreviousNodeBlock.PREVIOUS_NODE_BLOCK_TYPE,
            AgeBlock.AGE_BLOCK_TYPE,
            ScopeControlHopLimitBlock.SCOPE_CONTROL_HOP_LIMIT_BLOCK_TYPE
    };

    @Override
    public BlockProcessor create(int type) throws ProcessorNotFoundException {
        if (Arrays.asList(basicBlockTypes).contains(type)) {
            return nullProcessor;
        } else {
            throw new ProcessorNotFoundException();
        }
    }
}
