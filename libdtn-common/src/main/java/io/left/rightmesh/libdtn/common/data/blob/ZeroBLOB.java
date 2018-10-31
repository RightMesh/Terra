package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.common.data.Tag;
import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * ZeroBLOB is a zero-sized BLOB that holds no data and throws an exception when trying to
 * write data into it. It returns 0 for every read operation and observe completes immediately
 * without error.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class ZeroBLOB extends Tag implements BLOB {

    public ZeroBLOB() {
    }

    public ZeroBLOB(int expectedSize) {
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.empty();
    }

    @Override
    public long size() {
        return 0;
    }

    public class ZeroReadableBLOB implements ReadableBLOB {
        @Override
        public void read(OutputStream stream) throws IOException {
        }

        @Override
        public void close() {
        }
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ZeroReadableBLOB();
    }

    public class ZeroWritableBLOB implements WritableBLOB {
        @Override
        public void clear() {
        }

        @Override
        public int write(InputStream stream) throws BLOBOverflowException {
            throw new BLOBOverflowException();
        }

        @Override
        public int write(InputStream stream, int size) throws BLOBOverflowException {
            throw new BLOBOverflowException();
        }

        @Override
        public int write(byte b) throws BLOBOverflowException {
            throw new BLOBOverflowException();
        }

        @Override
        public int write(byte[] a) throws BLOBOverflowException {
            throw new BLOBOverflowException();
        }

        @Override
        public int write(ByteBuffer buffer) throws BLOBOverflowException {
            throw new BLOBOverflowException();
        }

        @Override
        public void close() {

        }
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new ZeroWritableBLOB();
    }

    @Override
    public boolean isFileBLOB() {
        return false;
    }

    @Override
    public String getFilePath() throws NotFileBLOB {
        throw new NotFileBLOB();
    }

    @Override
    public Completable moveToFile(String path) {
        return Completable.complete();
    }
}
