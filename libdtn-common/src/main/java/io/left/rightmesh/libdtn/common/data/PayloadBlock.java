package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.blob.Blob;
import io.left.rightmesh.libdtn.common.data.blob.UntrackedByteBufferBlob;

import java.nio.ByteBuffer;

/**
 * PayloadBlock is a CanonicalBlock that holds the payload of the Bundle.
 * It inherits the class {@link BlockBlob}.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class PayloadBlock extends BlockBlob {

    public static final int PAYLOAD_BLOCK_TYPE = 1;

    /**
     * Constructor: creates an empty PayloadBlock.
     */
    public PayloadBlock() {
        super(PAYLOAD_BLOCK_TYPE);
    }

    /**
     * Constructor: creates a PayloadBlock with a Blob as data.
     *
     * @param data payload
     */
    public PayloadBlock(Blob data) {
        super(PAYLOAD_BLOCK_TYPE, data);
    }

    /**
     * Constructor: creates a PayloadBlock with a Blob as data.
     *
     * @param data payload
     */
    public PayloadBlock(String data) {
        super(PAYLOAD_BLOCK_TYPE, new UntrackedByteBufferBlob(data.getBytes()));
    }


    /**
     * Constructor: creates a PayloadBlock with a byte array as data.
     *
     * @param data payload
     */
    public PayloadBlock(byte[] data) {
        super(PAYLOAD_BLOCK_TYPE, new UntrackedByteBufferBlob(data));
    }


    /**
     * Constructor: creates a PayloadBlock with a ByteBuffer as data.
     *
     * @param data payload
     */
    public PayloadBlock(ByteBuffer data) {
        super(PAYLOAD_BLOCK_TYPE, new UntrackedByteBufferBlob(data));
    }



}
