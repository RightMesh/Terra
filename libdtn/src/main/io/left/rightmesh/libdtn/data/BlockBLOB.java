package io.left.rightmesh.libdtn.data;

import io.left.rightmesh.libdtn.storage.BLOB;
import io.left.rightmesh.libdtn.storage.BundleStorage;
import io.left.rightmesh.libdtn.storage.NullBLOB;
import io.left.rightmesh.libdtn.storage.WritableBLOB;
import io.left.rightmesh.libdtn.utils.rxparser.RxParserException;
import io.left.rightmesh.libdtn.utils.rxparser.RxState;
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

    @Override
    public long getDataSize() {
        return data.size();
    }

    @Override
    public RxState parseData() {
        return blob_payload_parser;
    }

    private RxState blob_payload_parser = new RxState() {

        WritableBLOB writableData = null;

        @Override
        public void onEnter() throws RxParserException {
            try {
                data = BLOB.createBLOB((int) dataSize);
            } catch (BundleStorage.StorageFullException e) {
                throw new RxParserException("BlockBLOB", e.getMessage());
            }

            try {
                writableData = data.getWritableBLOB();
            } catch (BLOB.BLOBException be) {
                throw new RxParserException("BlockBLOB", be.getMessage());
            }
        }

        @Override
        public void onNext(ByteBuffer next) throws RxParserException {
            try {
                while (next.hasRemaining()) {
                    writableData.write(next.get());
                }
            } catch (IOException io) {
                throw new RxParserException("BlockBLOB", io.getMessage());
            } catch (WritableBLOB.BLOBOverflowException boe) {
                throw new RxParserException("BlockBLOB", boe.getMessage());
            }
        }

        @Override
        public void onExit() {
            writableData.close();
            writableData = null;
        }
    };

    @Override
    public Flowable<ByteBuffer> serializeData() {
        return data.observe();
    }
}
