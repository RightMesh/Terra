package io.left.rightmesh.libdtn.common.data;

import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.NullBLOB;

/**
 * A BlockBLOB is a generic CanonicalBlock with a BLOB object as payload.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class BlockBLOB extends CanonicalBlock {

    public BLOB data;

    /**
     * Constructor: creates an empty PayloadBlock.
     *
     * @param type block type
     */
    public BlockBLOB(int type) {
        super(type);
        data = new NullBLOB();
    }

    /**
     * Constructor: creates a PayloadBlock with a BLOB as data.
     *
     * @param type block type
     * @param data payload
     */
    public BlockBLOB(int type, BLOB data) {
        super(type);
        this.data = data;
    }

    /**
     * Clear the blob unless it is tagged not so
     */
    @Override
    public void clearBlock() {
        super.clearBlock();
        data.getWritableBLOB().clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BlockBLOB");
        if (data.size() != 0) {
            sb.append(": length=").append(data.size());
        }
        return sb.toString();
    }
}
