package io.left.rightmesh.libdtn.core.processor;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.data.CanonicalBlock;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.utils.ClockUtil;

import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DISCARD_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED;

/**
 * CoreProcessor processes a Bundle during the different step of its lifetime.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public class CoreProcessor {

    /**
     * Deserializer MUST call this method to ensure validity of the received Bundle
     *
     * @param bundle to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(Bundle bundle) throws RejectedException {
        onDeserialized((PrimaryBlock) bundle);
        for (CanonicalBlock block : bundle.getBlocks()) {
            onDeserialized(block);
        }
    }

    /**
     * Deserializer MUST call this method to ensure validity of the received PrimaryBlock early on.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(PrimaryBlock block) throws RejectedException {
        if (ClockUtil.isExpired(block.creationTimestamp, block.lifetime)) {
            throw new RejectedException("bundle is expired");
        }

        if (!DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_FORWARDING).value()
                && !LocalEIDTable.isLocal(block.destination)) {
            throw new RejectedException("forward isn't enabled and bundle is not local");
        }

        if (DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.EID_SINGLETON_ONLY).value()
                && !block.getV6Flag(PrimaryBlock.BundleV6Flags.DESTINATION_IS_SINGLETON)) {
            throw new RejectedException("bundle is not addressed to a singleton endpoint");
        }

        if (block.lifetime
                > DTNConfiguration.<Integer>get(DTNConfiguration.Entry.MAX_LIFETIME).value()) {
            throw new RejectedException("lifetime is too long");
        }

        if (DTNConfiguration.<Integer>get(
                DTNConfiguration.Entry.MAX_TIMESTAMP_FUTURE).value() > 0
                && block.creationTimestamp > ClockUtil.getCurrentTime()
                + DTNConfiguration.<Integer>get(
                DTNConfiguration.Entry.MAX_TIMESTAMP_FUTURE).value()) {
            throw new RejectedException("timestamp too far in the future");
        }
    }

    /**
     * Deserializer MUST call this method to ensure validity of the received BlockHeader.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(BlockHeader block) throws RejectedException {
        if (block.dataSize
                > DTNConfiguration.<Long>get(DTNConfiguration.Entry.LIMIT_BLOCKSIZE).value()) {
            throw new RejectedException("blocksize exceed limit");
        }
    }

    /**
     * Deserializer MUST call this method to ensure validity of the received CanonicalBlock.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(CanonicalBlock block) throws RejectedException {
        try {
            block.onBlockDataDeserialized();
        } catch (ProcessorNotFoundException pne) {
            if (block.getV7Flag(DELETE_BUNDLE_IF_NOT_PROCESSED)) {
                throw new RejectedException("mandatory block cannot be processed");
            }
        } catch (ProcessingException pe) {
            throw new RejectedException(pe.reason);
        }
    }
}
