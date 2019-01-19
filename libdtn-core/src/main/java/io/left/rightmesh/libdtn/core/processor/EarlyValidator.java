package io.left.rightmesh.libdtn.core.processor;

import static io.left.rightmesh.libdtn.common.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;

import io.left.rightmesh.libdtn.common.data.BlockHeader;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.PrimaryBlock;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.utils.ClockUtil;

/**
 * EarlyValidator processes a Bundle during the different step of its lifetime.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public class EarlyValidator {

    /**
     * RejectedException is raised if an error during processing must discard the entire bundle.
     *
     * @author Lucien Loiseau on 03/09/18.
     */
    public static class RejectedException extends ProcessingException {

        /**
         * Constructor.
         *
         * @param reason for not validating the block
         */
        public RejectedException(String reason) {
            super(reason);
        }
    }

    private CoreApi core;

    public EarlyValidator(CoreApi core) {
        this.core = core;
    }

    /**
     * Deserializer MAY call this method to ensure validity of the received PrimaryBlock early on.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public void onDeserialized(PrimaryBlock block) throws RejectedException {
        if (ClockUtil.isExpired(block.getCreationTimestamp(), block.getLifetime())) {
            throw new RejectedException("bundle is expired");
        }

        if (!core.getConf()
                .<Boolean>get(ConfigurationApi.CoreEntry.ALLOW_RECEIVE_ANONYMOUS_BUNDLE).value()
                && block.getSource().equals(DtnEid.nullEid())) {
            throw new RejectedException("forbidden anonnymous source");
        }

        if (!core.getLocalEid().isLocal(block.getDestination())
                && !core.getConf()
                .<Boolean>get(ConfigurationApi.CoreEntry.ENABLE_FORWARDING).value()) {
            throw new RejectedException("forward isn't enabled and bundle is not local");
        }

        long maxLifetime = core.getConf()
                .<Long>get(ConfigurationApi.CoreEntry.MAX_LIFETIME).value();
        if (block.getLifetime() > maxLifetime) {
            throw new RejectedException("lifetime=" + block.getLifetime() + " max=" + maxLifetime);
        }

        long maxTimestampFutur = ClockUtil.getCurrentTime()
                + core.getConf().<Long>get(ConfigurationApi.CoreEntry.MAX_TIMESTAMP_FUTURE).value();
        if (maxTimestampFutur > 0 && (block.getCreationTimestamp() > maxTimestampFutur)) {
            throw new RejectedException("timestamp too far in the future");
        }
    }

    /**
     * Deserializer MAY call this method to ensure validity of the received BlockHeader.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public void onDeserialized(BlockHeader block) throws RejectedException {
        if (block.dataSize
                > core.getConf().<Long>get(ConfigurationApi.CoreEntry.LIMIT_BLOCKSIZE).value()) {
            throw new RejectedException("block size exceed limit");
        }
    }

    /**
     * Deserializer MAY call this method to ensure validity of the received CanonicalBlock.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public void onDeserialized(CanonicalBlock block) throws RejectedException {
        try {
            core.getExtensionManager().getBlockProcessorFactory().create(block.type)
                    .onBlockDeserialized(block);
        } catch (BlockProcessorFactory.ProcessorNotFoundException pne) {
            if (block.getV7Flag(DELETE_BUNDLE_IF_NOT_PROCESSED)) {
                throw new RejectedException("mandatory block cannot be processed");
            }
        } catch (ProcessingException pe) {
            throw new RejectedException(pe.getMessage());
        }
    }
}
