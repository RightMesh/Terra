package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.NullBLOB;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BlockBLOB");
        if (data.size() != 0) {
            sb.append(": length=").append(data.size());
        }
        return sb.toString();
    }
}
