package io.left.rightmesh.libdtn.common.data;

import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.ByteBufferBLOB;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBLOB;

/**
 * PayloadBlock is a CanonicalBlock that holds the payload of the Bundle.
 * It inherits the class {@link BlockBLOB}.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class PayloadBlock extends BlockBLOB {

    public static final int type = 1;

    /**
     * Constructor: creates an empty PayloadBlock.
     */
    public PayloadBlock() {
        super(type);
    }

    /**
     * Constructor: creates a PayloadBlock with a BLOB as data.
     *
     * @param data payload
     */
    public PayloadBlock(BLOB data) {
        super(type, data);
    }

    /**
     * Constructor: creates a PayloadBlock with a BLOB as data.
     *
     * @param data payload
     */
    public PayloadBlock(String data) {
        super(type, new UntrackedByteBufferBLOB(data.getBytes()));
    }


    /**
     * Constructor: creates a PayloadBlock with a byte array as data.
     *
     * @param data payload
     */
    public PayloadBlock(byte[] data) {
        super(type, new UntrackedByteBufferBLOB(data));
    }


    /**
     * Constructor: creates a PayloadBlock with a ByteBuffer as data.
     *
     * @param data payload
     */
    public PayloadBlock(ByteBuffer data) {
        super(type, new UntrackedByteBufferBLOB(data));
    }



}
