package io.left.rightmesh.libdtn.common.data;

/**
 * Generic block header object. It is the super class of {@link CanonicalBlock}.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class BlockHeader extends Block {

    public enum CrcFieldType {
        NO_CRC,
        CRC_16,
        CRC_32
    }

    public enum BlockV7Flags {
        /* CanonicalBlock Processing Control Flags
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
        BLOCK_IS_ENCRYPTED, // not in RFC
        RESERVED_4
    }

    public int type;
    public int number;
    public long procV7flags = 0;
    public CrcFieldType crcType;
    public long dataSize;


    /**
     * Constructor creates a BlockHeader of the following PAYLOAD_BLOCK_TYPE.
     *
     * @param type of the current BlockHeader
     */
    public BlockHeader(int type) {
        this.type = type;
        crcType = CrcFieldType.NO_CRC;
    }

    /**
     * BlockHeader copy constructor.
     *
     * @param header to copy
     */
    public BlockHeader(BlockHeader header) {
        this.type = header.type;
        this.number = header.number;
        this.procV7flags = header.procV7flags;
        this.crcType = header.crcType;
        this.dataSize = header.dataSize;
    }

    /**
     * Get the state of a specific {@link BlockHeader.BlockV7Flags}.
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
    public void setV7Flag(BlockV7Flags flag, boolean state) {
        if (state) {
            procV7flags |= (1 << flag.ordinal());
        } else {
            procV7flags &= ~(1 << flag.ordinal());
        }
    }
}
