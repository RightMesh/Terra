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

    public enum BlockV6Flags {
        REPLICATE_IN_EVERY_FRAGMENT,
        TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED,
        DELETE_BUNDLE_IF_NOT_PROCESSED,
        LAST_BLOCK,
        DISCARD_IF_NOT_PROCESSED,
        FORWARDED_WITHOUT_PROCESSED,
        BLOCK_CONTAINS_EIDS
    }

    public enum CRCFieldType {
        NO_CRC,
        CRC_16,
        CRC_32
    }

    public enum BlockV7Flags {
        /* Block Processing Control Flags
             . Bit 0 (the high-order bit, 0x80): reserved.
             . Bit 1 (0x40): reserved.
             . Bit 2 (0x20): reserved.
             . Bit 3 (0x10): reserved.
             . Bit 4 (0x08): bundle must be deleted if block can't be
                             processed.
             . Bit 5 (0x04): transmission of a status report is requested if
                             block can't be processed.
             . Bit 6 (0x02): block must be removed from bundle if it can't be
                             processed.
             . Bit 7 (the low-order bit, 0x01): block must be replicated in
                                                every fragment.
         */
        REPLICATE_IN_EVERY_FRAGMENT,
        DISCARD_IF_NOT_PROCESSED,
        TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED,
        DELETE_BUNDLE_IF_NOT_PROCESSED,
        RESERVED_1,
        RESERVED_2,
        RESERVED_3,
        RESERVED_4
    }

    public int type;
    public int number;
    public CRCFieldType crcType;
    public long procV6flags = 0;
    public long procV7flags = 0;
    public long dataSize;
    public HashSet<EID> eids = new HashSet<>();

    /** processing field for deserialization **/
    public boolean crc_ok;

    /**
     * Constructor creates a BlockHeader of the following type.
     *
     * @param type of the current BlockHeader
     */
    public BlockHeader(int type) {
        this.type = type;
        crcType = CRCFieldType.NO_CRC;
        this.crc_ok = true;
    }

    /**
     * Get the state of a specific {@see BlockHeader.BlockV6Flags}.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getV6Flag(BlockV6Flags flag) {
        return ((procV6flags >> flag.ordinal()) & 0x1) == 0x1;
    }

    /**
     * Get the state of a specific {@see BlockHeader.BlockV7Flags}.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getV7Flag(BlockV7Flags flag) {
        return ((procV7flags >> flag.ordinal()) & 0x1) == 0x1;
    }

    /**
     * Set/clear a flag on this BlockHeader.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    public void setV6Flag(BlockV6Flags flag, boolean state) {
        if (flag == BlockV6Flags.BLOCK_CONTAINS_EIDS) {
            return;
        }

        if (state) {
            procV6flags |= (1 << flag.ordinal());
        } else {
            procV6flags &= ~(1 << flag.ordinal());
        }
    }

    /**
     * Set/clear a flag on this BlockHeader.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    public void setV7Flag(BlockV7Flags flag, boolean state) {
        if (state) {
            procV7flags |= (1 << flag.ordinal());
        } else {
            procV7flags &= ~(1 << flag.ordinal());
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
            setV6Flag(BlockV6Flags.BLOCK_CONTAINS_EIDS, false);
        } else {
            setV6Flag(BlockV6Flags.BLOCK_CONTAINS_EIDS, true);
        }
    }
}
