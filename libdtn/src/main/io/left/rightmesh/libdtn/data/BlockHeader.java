package io.left.rightmesh.libdtn.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic block header object. It is the super class of {@see Block}.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class BlockHeader {

    public enum BlockFlags {
        REPLICATE_IN_EVERY_FRAGMENT,
        TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED,
        DELETE_BUNDLE_IF_NOT_PROCESSED,
        LAST_BLOCK,
        DISCARD_IF_NOT_PROCESSED,
        FORWARDED_WITHOUT_PROCESSED,
        BLOCK_CONTAINS_EIDS
    }

    public int type;
    public long procflags = 0;
    public long dataSize;
    public HashSet<EID> eids = new HashSet<>();

    /**
     * Constructor creates a BlockHeader of the following type.
     *
     * @param type of the current BlockHeader
     */
    public BlockHeader(int type) {
        this.type = type;
    }

    /**
     * Get the state of a specific {@see BlockHeader.BlockFlags}.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getFlag(BlockFlags flag) {
        return ((procflags >> flag.ordinal()) & 0x1) == 0x1;
    }

    /**
     * Set/clear a flag on this BlockHeader.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    public void setFlag(BlockFlags flag, boolean state) {
        if (flag == BlockFlags.BLOCK_CONTAINS_EIDS) {
            return;
        }

        if (state) {
            procflags |= (1 << flag.ordinal());
        } else {
            procflags &= ~(1 << flag.ordinal());
        }
    }

    /**
     * addEID adds an EID to this Bundle.
     *
     * @param eid to be added
     */
    public void addEID(EID eid) {
        eids.add(eid);
        fixEIDFlag();
    }

    /**
     * Set the EIDs in this bundle to the given Set.
     *
     * @param eids the eid to set
     */
    public void setEID(HashSet<EID> eids) {
        this.eids = eids;
        fixEIDFlag();
    }


    private void fixEIDFlag() {
        if (eids.isEmpty()) {
            setFlag(BlockFlags.BLOCK_CONTAINS_EIDS, false);
        } else {
            setFlag(BlockFlags.BLOCK_CONTAINS_EIDS, true);
        }
    }
}
