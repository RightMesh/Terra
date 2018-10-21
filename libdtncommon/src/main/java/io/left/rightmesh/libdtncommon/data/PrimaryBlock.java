package io.left.rightmesh.libdtncommon.data;


import io.left.rightmesh.libdtncommon.data.eid.DTN;
import io.left.rightmesh.libdtncommon.data.eid.EID;

/**
 * PrimaryBlock is the first block of a Bundle (RFC5050), there can be only one per Bundle.
 * The primary bundle block contains the basic information needed to route bundles
 * to their destinations. It is a super class of {@see Bundle}
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class PrimaryBlock extends Block {

    public enum Priority {
        BULK,
        NORMAL,
        EXPEDITED
    }

    public enum CRCFieldType {
        NO_CRC,
        CRC_16,
        CRC_32
    }

    public static long sequence_counter = 0;

    public enum BundleV6Flags {
        FRAGMENT(0),
        ADM_RECORD(1),
        NO_FRAGMENT(2),
        CUSTODY_REQUEST(3),
        DESTINATION_IS_SINGLETON(4),
        APP_ACK_REQUEST(5),
        RESERVED_6(6),
        PRIORITY_BIT1(7),
        PRIORITY_BIT2(8),
        CLASSOFSERVICE_9(9),
        CLASSOFSERVICE_10(10),
        CLASSOFSERVICE_11(11),
        CLASSOFSERVICE_12(12),
        CLASSOFSERVICE_13(13),
        RECEPTION_REPORT(14),
        CUSTODY_REPORT(15),
        FORWARD_REPORT(16),
        DELIVERY_REPORT(17),
        DELETION_REPORT(18);

        private int offset;

        BundleV6Flags(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }

    public enum BundleV7Flags {
        /* BundleV7 Processing Control Flag {@href https://tools.ietf.org/html/draft-ietf-dtn-bpbis-11#section-4.1.3}
            . Bit 0 (the high-order bit, 0x8000): reserved.
            . Bit 1 (0x4000): reserved.
            . Bit 2 (0x2000): reserved.
            . Bit 3(0x1000): bundle deletion status reports are requested.
            . Bit 4(0x0800): bundle delivery status reports are requested.
            . Bit 5(0x0400): bundle forwarding status reports are requested.
            . Bit 6(0x0200): reserved.
            . Bit 7(0x0100): bundle reception status reports are requested.
            . Bit 8(0x0080): bundle contains a Manifest block.
            . Bit 9(0x0040): status time is requested in all status reports.
            . Bit 10(0x0020): user application acknowledgement is requested.
            . Bit 11(0x0010): reserved.
            . Bit 12(0x0008): reserved.
            . Bit 13(0x0004): bundle must not be fragmented.
            . Bit 14(0x0002): payload is an administrative record.
            . Bit 15 (the low-order bit, 0x0001: bundle is a fragment.
         */

        FRAGMENT(0),
        ADM_RECORD(1),
        NO_FRAGMENT(2),
        RESERVED_1(3),
        RESERVED_2(4),
        APP_ACK_REQUEST(5),
        STATUS_TIME_REPORT(6),
        CONTAINS_MANIFEST(7),
        RECEPTION_REPORT(8),
        RESERVED_3(9),
        FORWARD_REPORT(10),
        DELIVERY_REPORT(11),
        DELETION_REPORT(12),
        RESERVED_4(13),
        RESERVED_5(14),
        RESERVED_6(15);

        private int offset;

        BundleV7Flags(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }

    /** BPv6 and BPv7 fields */
    public int version;
    public long procV6Flags;
    public long procV7Flags;
    public CRCFieldType crcType;
    public EID destination;
    public EID source;
    public EID reportto;
    public EID custodian;
    public long creationTimestamp;
    public long sequenceNumber;
    public long lifetime;
    public Long appDataLength = null;
    public Long fragmentOffset = null;

    /** libdtn internal use **/
    public BundleID bid;

    /**
     * Constructor: creates an empty PrimaryBlock, should probably not be used.
     */
    public PrimaryBlock() {
        this.procV6Flags = 0;
        this.procV7Flags = 0;
        this.crcType = CRCFieldType.NO_CRC;
        this.destination = DTN.NullEID();
        this.source = DTN.NullEID();
        this.custodian = DTN.NullEID();
        this.reportto = DTN.NullEID();
        this.creationTimestamp = System.currentTimeMillis();
        this.sequenceNumber = sequence_counter++;
        bid = BundleID.create(this);
        this.lifetime = 10000;
    }

    /**
     * Constructor: creates a PrimaryBlock with minimum information.
     *
     * @param destination EID of this Bundle
     * @param lifetime    of this Bundle
     */
    public PrimaryBlock(EID destination, long lifetime) {
        this();
        this.lifetime = lifetime;
        this.destination = destination;
    }

    /**
     * Constructor: creates a PrimaryBlock from another one.
     *
     * @param other other primary block to copy
     */
    public PrimaryBlock(PrimaryBlock other) {
        this.procV6Flags = other.procV6Flags;
        this.procV7Flags = other.procV7Flags;
        this.source = other.source;
        this.destination = other.destination;
        this.reportto = other.reportto;
        this.custodian = other.custodian;
        this.creationTimestamp = other.creationTimestamp;
        this.sequenceNumber = other.sequenceNumber;
        this.lifetime = other.lifetime;
        this.appDataLength = other.appDataLength;
        this.fragmentOffset = other.fragmentOffset;
        this.bid = other.bid;
    }

    /**
     * returns the sdnv_value of a specific {@see PrimaryBlock.BundleV6Flags} for this Bundle.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getV6Flag(BundleV6Flags flag) {
        return (flag.getOffset() & this.procV6Flags) > 0;
    }

    /**
     * returns the sdnv_value of a specific {@see PrimaryBlock.BundleV7Flags} for this Bundle.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getV7Flag(BundleV7Flags flag) {
        return (flag.getOffset() & this.procV7Flags) > 0;
    }

    /**
     * getPriority returns the {@see PrimaryBlock.Priority} of this Bundle.
     *
     * @return Priority of the Bundle
     */
    public Priority getV6Priority() {
        if (getV6Flag(BundleV6Flags.PRIORITY_BIT1)) {
            return Priority.NORMAL;
        }
        if (getV6Flag(BundleV6Flags.PRIORITY_BIT2)) {
            return Priority.EXPEDITED;
        }
        return Priority.BULK;
    }

    /**
     * setFlag set (or unset) a given {@see PrimaryBlock.BundleFlags}.
     *
     * @param flag  to be set
     * @param value of the flag: true to set, false to unset
     */
    public void setV6Flag(BundleV6Flags flag, boolean value) {
        if (value) {
            procV6Flags |= 0b1L << flag.getOffset();
        } else {
            procV6Flags &= ~(0b1L << flag.getOffset());
        }
    }

    /**
     * setFlag set (or unset) a given {@see PrimaryBlock.BundleFlags}.
     *
     * @param flag  to be set
     * @param value of the flag: true to set, false to unset
     */
    public void setV7Flag(BundleV7Flags flag, boolean value) {
        if (value) {
            procV7Flags |= 0b1L << flag.getOffset();
        } else {
            procV7Flags &= ~(0b1L << flag.getOffset());
        }
    }

    /**
     * set the {@see PrimaryBlock.Priority} for the given Bundle.
     *
     * @param p the priority of the Bundle
     */
    public void setV6Priority(Priority p) {
        switch (p) {
            case BULK:
                setV6Flag(BundleV6Flags.PRIORITY_BIT1, false);
                setV6Flag(BundleV6Flags.PRIORITY_BIT2, false);
                break;

            case EXPEDITED:
                setV6Flag(BundleV6Flags.PRIORITY_BIT1, false);
                setV6Flag(BundleV6Flags.PRIORITY_BIT2, true);
                break;

            case NORMAL:
                setV6Flag(BundleV6Flags.PRIORITY_BIT1, true);
                setV6Flag(BundleV6Flags.PRIORITY_BIT2, false);
                break;
            default:
                break;
        }
    }

}
