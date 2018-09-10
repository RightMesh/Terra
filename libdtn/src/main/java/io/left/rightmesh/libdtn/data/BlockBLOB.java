package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.BundleStorage;
import io.left.rightmesh.libdtn.storage.NullBLOB;
import io.left.rightmesh.libdtn.storage.WritableBLOB;
import io.left.rightmesh.libdtn.utils.rxparser.ParserState;
import io.left.rightmesh.libdtn.utils.rxparser.RxParserException;
import io.reactivex.Flowable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A BlockBLOB is a generic Block with a BLOB object as payload.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class BlockBLOB extends Block {

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
