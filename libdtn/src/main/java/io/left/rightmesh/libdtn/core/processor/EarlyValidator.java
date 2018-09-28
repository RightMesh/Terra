package io.left.rightmesh.libdtn.core.processor;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.data.CanonicalBlock;
import io.left.rightmesh.libdtn.data.BlockHeader;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.data.PrimaryBlock;
import io.left.rightmesh.libdtn.utils.ClockUtil;

import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DELETE_BUNDLE_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.DISCARD_IF_NOT_PROCESSED;
import static io.left.rightmesh.libdtn.data.BlockHeader.BlockV7Flags.TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED;

/**
 * EarlyValidator processes a Bundle during the different step of its lifetime.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public class EarlyValidator {

    /**
     * Deserializer MAY call this method to ensure validity of the received PrimaryBlock early on.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(PrimaryBlock block) throws RejectedException {
        if (ClockUtil.isExpired(block.creationTimestamp, block.lifetime)) {
            throw new RejectedException("bundle is expired");
        }

        if (!DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ALLOW_RECEIVE_ANONYMOUS_BUNDLE).value()
                && block.source.equals(EID.NullEID())) {
            throw new RejectedException("forbidden anonnymous source");
        }

        if (!LocalEIDTable.isLocal(block.destination)
                && !DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_FORWARDING).value()) {
            throw new RejectedException("forward isn't enabled and bundle is not local");
        }

        long max_lifetime = DTNConfiguration.<Integer>get(DTNConfiguration.Entry.MAX_LIFETIME).value();
        if (block.lifetime > max_lifetime) {
            throw new RejectedException("lifetime="+block.lifetime+" max="+max_lifetime);
        }

        long max_timestamp_futur = DTNConfiguration.<Integer>get(DTNConfiguration.Entry.MAX_TIMESTAMP_FUTURE).value();
        if (max_timestamp_futur > 0
                && (block.creationTimestamp > ClockUtil.getCurrentTime() + max_timestamp_futur)) {
            throw new RejectedException("timestamp too far in the future");
        }
    }

    /**
     * Deserializer MAY call this method to ensure validity of the received BlockHeader.
     *
     * @param block to test validity
     * @throws RejectedException if the bundle is to be rejected
     */
    public static void onDeserialized(BlockHeader block) throws RejectedException {
        if (block.dataSize
                > DTNConfiguration.<Long>get(DTNConfiguration.Entry.LIMIT_BLOCKSIZE).value()) {
            throw new RejectedException("block size exceed limit");
        }
    }

    /**
     * Deserializer MAY call this method to ensure validity of the received CanonicalBlock.
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
