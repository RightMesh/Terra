package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.storage.BLOB;

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

}
