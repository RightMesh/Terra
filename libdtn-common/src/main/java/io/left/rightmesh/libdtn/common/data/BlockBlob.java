package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.blob.Blob;
import io.left.rightmesh.libdtn.common.data.blob.NullBlob;

/**
 * A BlockBlob is a generic CanonicalBlock with a Blob object as payload.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class BlockBlob extends CanonicalBlock {

    public Blob data;

    /**
     * Constructor: creates an empty PayloadBlock.
     *
     * @param type block PAYLOAD_BLOCK_TYPE
     */
    public BlockBlob(int type) {
        super(type);
        data = new NullBlob();
    }

    /**
     * Constructor: creates a PayloadBlock with a Blob as data.
     *
     * @param type block PAYLOAD_BLOCK_TYPE
     * @param data payload
     */
    public BlockBlob(int type, Blob data) {
        super(type);
        this.data = data;
    }

    public BlockBlob(CanonicalBlock block) {
        super(block);
        data = new NullBlob();
    }

    /**
     * Clear the blob unless it is tagged not so.
     */
    @Override
    public void clearBlock() {
        super.clearBlock();
        data.getWritableBlob().clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BlockBlob");
        if (data.size() != 0) {
            sb.append(": length=").append(data.size());
        }
        return sb.toString();
    }
}
