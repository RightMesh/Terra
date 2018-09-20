package io.left.rightmesh.libdtn.data;

import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.ByteBufferBLOB;

/**
 * PayloadBlock is a Block that holds the payload of the Bundle.
 * It inherits the class {@see BlockBLOB}.
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
        super(type, new ByteBufferBLOB(data.getBytes()));
    }


    /**
     * Constructor: creates a PayloadBlock with a byte array as data.
     *
     * @param data payload
     */
    public PayloadBlock(byte[] data) {
        super(type, new ByteBufferBLOB(data));
    }


    /**
     * Constructor: creates a PayloadBlock with a ByteBuffer as data.
     *
     * @param data payload
     */
    public PayloadBlock(ByteBuffer data) {
        super(type, new ByteBufferBLOB(data));
    }



}
