package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.eid.ApiEid;
import io.left.rightmesh.libdtn.common.data.eid.DtnEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;

/**
 * PrimaryBlock is the first block of a {@link Bundle}, there can be only one per Bundle.
 * The primary bundle block contains the basic information needed to route bundles
 * to their destinations.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class PrimaryBlock extends Block {

    public enum Priority {
        BULK,
        NORMAL,
        EXPEDITED
    }

    public enum CrcFieldType {
        NO_CRC,
        CRC_16,
        CRC_32
    }

    private static long sequence_counter = 0;

    public enum BundleV7Flags {
        /* BundleV7 Processing Control Flag {@link https://tools.ietf.org/html/draft-ietf-dtn-bpbis-11#section-4.1.3}
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

    /* BPv6 and BPv7 fields */
    private int version;
    private long procV7Flags;
    private CrcFieldType crcType;
    private Eid destination;
    private Eid source;
    private Eid reportto;
    private long creationTimestamp;
    private long sequenceNumber;
    private long lifetime;
    private Long appDataLength = null;
    private Long fragmentOffset = null;

    /* libdtn internal use */
    public BundleId bid;

    /**
     * Constructor: creates an empty PrimaryBlock, should probably not be used.
     */
    public PrimaryBlock() {
        this.procV7Flags = 0;
        this.crcType = CrcFieldType.NO_CRC;
        this.destination = DtnEid.nullEid();
        this.source = ApiEid.me();
        this.reportto = DtnEid.nullEid();
        this.creationTimestamp = System.currentTimeMillis();
        this.sequenceNumber = sequence_counter++;
        bid = BundleId.create(this);
        this.lifetime = 10000;
    }

    /**
     * Constructor: creates a PrimaryBlock with minimum information.
     *
     * @param destination Eid of this Bundle
     * @param lifetime    of this Bundle
     */
    public PrimaryBlock(Eid destination, long lifetime) {
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
        this.procV7Flags = other.procV7Flags;
        this.source = other.source;
        this.destination = other.destination;
        this.reportto = other.reportto;
        this.creationTimestamp = other.creationTimestamp;
        this.sequenceNumber = other.sequenceNumber;
        this.lifetime = other.lifetime;
        this.appDataLength = other.appDataLength;
        this.fragmentOffset = other.fragmentOffset;
        this.bid = other.bid;
    }

    public int getVersion() {
        return version;
    }

    public long getProcV7Flags() {
        return procV7Flags;
    }

    /**
     * returns the sdnv_value of a specific {@link PrimaryBlock.BundleV7Flags} for this Bundle.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getV7Flag(BundleV7Flags flag) {
        return ((0b1L << flag.getOffset()) & this.procV7Flags) > 0;
    }

    public CrcFieldType getCrcType() {
        return crcType;
    }

    public Eid getDestination() {
        return destination;
    }

    public Eid getSource() {
        return source;
    }

    public Eid getReportto() {
        return reportto;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public BundleId getBid() {
        return bid;
    }

    public long getLifetime() {
        return lifetime;
    }

    public Long getAppDataLength() {
        return appDataLength;
    }

    public Long getFragmentOffset() {
        return fragmentOffset;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setProcV7Flags(long procV7Flags) {
        this.procV7Flags = procV7Flags;
    }

    /**
     * set (or unset) a given {@link PrimaryBlock.BundleV7Flags}.
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

    public void setCrcType(CrcFieldType crcType) {
        this.crcType = crcType;
    }

    public void setDestination(Eid destination) {
        this.destination = destination;
    }

    public void setSource(Eid source) {
        this.source = source;
        this.bid = BundleId.create(this);
    }

    public void setReportto(Eid reportto) {
        this.reportto = reportto;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        this.bid = BundleId.create(this);
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.bid = BundleId.create(this);
    }

    public void setLifetime(long lifetime) {
        this.lifetime = lifetime;
    }

    public void setAppDataLength(Long appDataLength) {
        this.appDataLength = appDataLength;
    }

    public void setFragmentOffset(Long fragmentOffset) {
        this.fragmentOffset = fragmentOffset;
    }
}
