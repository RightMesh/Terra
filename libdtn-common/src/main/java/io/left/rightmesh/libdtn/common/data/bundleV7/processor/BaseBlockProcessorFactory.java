package io.left.rightmesh.libdtn.common.data.bundleV7.processor;

import java.util.Arrays;

import io.left.rightmesh.libdtn.common.data.AgeBlock;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.FlowLabelBlock;
import io.left.rightmesh.libdtn.common.data.ManifestBlock;
import io.left.rightmesh.libdtn.common.data.PayloadBlock;
import io.left.rightmesh.libdtn.common.data.PreviousNodeBlock;
import io.left.rightmesh.libdtn.common.data.ScopeControlHopLimitBlock;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 26/11/18.
 */
public class BaseBlockProcessorFactory implements BlockProcessorFactory {

    static BlockProcessor nullProcessor = new BlockProcessor() {
        @Override
        public void onBlockDataDeserialized(CanonicalBlock block) throws ProcessingException {
        }

        @Override
        public boolean onReceptionProcessing(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPutOnStorage(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPullFromStorage(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException {
            return false;
        }

        @Override
        public boolean onPrepareForTransmission(CanonicalBlock block, Bundle bundle, Log logger) throws ProcessingException {
            return false;
        }
    };

    static Integer[] basicBlockTypes = {
            PayloadBlock.type,
            ManifestBlock.type,
            FlowLabelBlock.type,
            PreviousNodeBlock.type,
            AgeBlock.type,
            ScopeControlHopLimitBlock.type
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
