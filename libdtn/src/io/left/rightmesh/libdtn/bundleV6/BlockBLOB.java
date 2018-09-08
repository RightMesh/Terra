package io.left.rightmesh.libdtn.bundleV6;

import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.BundleStorage;
import io.left.rightmesh.libdtn.storage.NullBLOB;
import io.left.rightmesh.libdtn.storage.WritableBLOB;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxDeserializerException;
import io.left.rightmesh.libdtn.utils.rxdeserializer.RxState;
import io.reactivex.Flowable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A BlockBLOB is a generic Block with a BLOB object as payload.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class BlockBLOB extends Block {

    private BLOB data;

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
     * get the {@see BLOB} object encapsulated in the current block.
     *
     * @return Data
     */
    public BLOB getData() {
        return data;
    }

    @Override
    public long getDataSize() {
        return data.size();
    }

    /**
     * set the BLOB for this block.
     *
     * @param data the BLOB to set for this block
     */
    public void setData(BLOB data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BlockBLOB");
        if (getDataSize() != 0) {
            sb.append(": length=").append(getDataSize());
        }
        return sb.toString();
    }

    @Override
    public RxState deserializeBlockData() {
        return blob_block_payload;
    }

    private RxState blob_block_payload = new RxState() {

        WritableBLOB writableData = null;

        @Override
        public void onEnter() throws RxDeserializerException {
            try {
                data = BLOB.createBLOB((int) dataSize);
            } catch (BundleStorage.StorageFullException e) {
                throw new RxDeserializerException("BlockBLOB", e.getMessage());
            }

            try {
                writableData = data.getWritableBLOB();
            } catch (BLOB.BLOBException be) {
                throw new RxDeserializerException("BlockBLOB", be.getMessage());
            }
        }

        @Override
        public void onNext(ByteBuffer next) throws RxDeserializerException {
            try {
                while (next.hasRemaining()) {
                    writableData.write(next.get());
                }
            } catch (IOException io) {
                throw new RxDeserializerException("BlockBLOB", io.getMessage());
            } catch (WritableBLOB.BLOBOverflowException boe) {
                throw new RxDeserializerException("BlockBLOB", boe.getMessage());
            }
        }

        @Override
        public void onExit() {
            writableData.close();
            writableData = null;
        }
    };

    @Override
    public Flowable<ByteBuffer> serializeBlockData() {
        return data.observe();
    }
}
