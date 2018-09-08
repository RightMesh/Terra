package io.left.rightmesh.libdtn.data;



/**
 * PrimaryBlock is the first block of a Bundle (RFC5050), there can be only one per Bundle.
 * The primary bundle block contains the basic information needed to route bundles
 * to their destinations. It is a super class of {@see Bundle}
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class PrimaryBlock {

    public static final byte BUNDLE_VERSION_6 = 0x06;
    public static final byte BUNDLE_VERSION_7 = 0x07;

    public enum Priority {
        BULK,
        NORMAL,
        EXPEDITED
    }

    public enum BundleFlags {
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

        BundleFlags(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
    }

    /** RFC5050 fields */
    public int version;
    public long procFlags;
    public EID destination;
    public EID source;
    public EID reportto;
    public EID custodian;
    public long creationTimestamp;
    public long sequenceNumber;
    public long lifetime;
    public Long appDataLength = null;
    public Long fragmentOffset = null;

    /**
     * Constructor: creates an empty PrimaryBlock, should probably not be used.
     */
    public PrimaryBlock() {
        this.procFlags = 0;
        this.destination = EID.NullEID();
        this.source = EID.NullEID();
        this.custodian = EID.NullEID();
        this.reportto = EID.NullEID();
        this.creationTimestamp = System.currentTimeMillis();
        this.sequenceNumber = (long)(Math.random() % Long.MAX_VALUE);
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
        this.procFlags = other.procFlags;
        this.source = other.source;
        this.destination = other.destination;
        this.reportto = other.reportto;
        this.custodian = other.custodian;
        this.creationTimestamp = other.creationTimestamp;
        this.sequenceNumber = other.sequenceNumber;
        this.lifetime = other.lifetime;
        this.appDataLength = other.appDataLength;
        this.fragmentOffset = other.fragmentOffset;
    }

    /**
     * returns the sdnv_value of a specific {@see PrimaryBlock.BundleFlags} for this Bundle.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getFlag(BundleFlags flag) {
        return (flag.getOffset() & this.procFlags) > 0;
    }

    /**
     * getPriority returns the {@see PrimaryBlock.Priority} of this Bundle.
     *
     * @return Priority of the Bundle
     */
    public Priority getPriority() {
        if (getFlag(BundleFlags.PRIORITY_BIT1)) {
            return Priority.NORMAL;
        }
        if (getFlag(BundleFlags.PRIORITY_BIT2)) {
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
    public void setFlag(BundleFlags flag, boolean value) {
        if (value) {
            procFlags |= 0b1L << flag.getOffset();
        } else {
            procFlags &= ~(0b1L << flag.getOffset());
        }
    }

    /**
     * set the {@see PrimaryBlock.Priority} for the given Bundle.
     *
     * @param p the priority of the Bundle
     */
    public void setPriority(Priority p) {
        switch (p) {
            case BULK:
                setFlag(BundleFlags.PRIORITY_BIT1, false);
                setFlag(BundleFlags.PRIORITY_BIT2, false);
                break;

            case EXPEDITED:
                setFlag(BundleFlags.PRIORITY_BIT1, false);
                setFlag(BundleFlags.PRIORITY_BIT2, true);
                break;

            case NORMAL:
                setFlag(BundleFlags.PRIORITY_BIT1, true);
                setFlag(BundleFlags.PRIORITY_BIT2, false);
                break;
            default:
                break;
        }
    }

}
