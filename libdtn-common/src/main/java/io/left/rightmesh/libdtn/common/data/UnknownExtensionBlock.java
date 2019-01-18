package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.blob.Blob;

/**
 * UnknownExtensionBlock is used to create a generic Extension CanonicalBlock in case when a block
 * type is unknown.
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class UnknownExtensionBlock extends BlockBlob {

    /**
     * Constructor: creates an empty UnknownExtensionBlock.
     *
     * @param type of the block
     */
    public UnknownExtensionBlock(int type) {
        super(type);
    }

    /**
     * Constructor: creates an UnknownExtensionBlock with a Blob as data.
     *
     * @param type of the block
     * @param data payload
     */
    public UnknownExtensionBlock(int type, Blob data) {
        super(type, data);
    }
}
