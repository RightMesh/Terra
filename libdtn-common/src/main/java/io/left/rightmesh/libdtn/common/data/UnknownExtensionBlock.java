package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.blob.BLOB;

/**
 * UnknownExtensionBlock is used to create a generic Extension CanonicalBlock in case when a block type is
 * unknown.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class UnknownExtensionBlock extends BlockBLOB {

    /**
     * Constructor: creates an empty UnknownExtensionBlock.
     *
     * @param type of the block
     */
    public UnknownExtensionBlock(int type) {
        super(type);
    }

    /**
     * Constructor: creates an UnknownExtensionBlock with a BLOB as data.
     *
     * @param type of the block
     * @param data payload
     */
    public UnknownExtensionBlock(int type, BLOB data) {
        super(type, data);
    }
}
