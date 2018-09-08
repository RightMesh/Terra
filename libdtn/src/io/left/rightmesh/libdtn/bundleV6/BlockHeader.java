package io.left.rightmesh.libdtn.bundleV6;

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

    protected int type;
    protected long procflags = 0;
    protected long dataSize;
    protected HashSet<EID> eids = new HashSet<>();

    /**
     * Constructor creates a BlockHeader of the following type.
     *
     * @param type of the current BlockHeader
     */
    public BlockHeader(int type) {
        this.type = type;
    }

    /**
     * get processing flags.
     *
     * @return processing flags as a long
     */
    public long getProcflags() {
        return procflags;
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
     * Get the type of the current block.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }


    /**
     * get the size of the block-type specific data field.
     *
     * @return size of the data
     */
    public long getDataSize() {
        return dataSize;
    }

    /**
     * Get a unmodifiable Set of EIDs associated with this BlockHeader.
     *
     * @return the EIDs of this BlockHeader
     */
    public final Set<EID> getEids() {
        return Collections.unmodifiableSet(eids);
    }

    /**
     * set the size of the block-type specific data field.
     *
     * @param size of the data
     */
    public void setDataSize(long size) {
        this.dataSize = size;
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
     * Sets flags on this BlockHeader. Multiple flags can be concatenated and separated
     * by whitespaces.
     *
     * <p>Flag.Offset.BLOCK_CONTAINS_EIDS is ignored and will automatically be set by addEID and
     * removeEID.
     *
     * @param value the flags to be set/cleared
     */
    public void setFlags(String value) {
        String[] flags = value.split(" ");
        for (String flag : flags) {
            BlockFlags offset = BlockFlags.valueOf(flag);
            setFlag(offset, true);
        }
    }

    /**
     * Sets flags on this BlockHeader.
     *
     * @param flags the flags to be set/cleared
     */
    public void setFlags(long flags) {
        this.procflags = flags;
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
     * addEID remove an EID from this Bundle.
     *
     * @param eid to be removed
     */
    public void removeEID(EID eid) {
        eids.remove(eid);
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
