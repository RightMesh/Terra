package io.left.rightmesh.libdtn.common.data.blob;

import io.left.rightmesh.libdtn.common.data.Tag;
import io.left.rightmesh.libdtn.common.utils.Function;
import io.left.rightmesh.libdtn.common.utils.Supplier;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * ZeroBlob is a zero-sized Blob that holds no data and throws an exception when trying to
 * write data into it. It returns 0 for every read operation and observe completes immediately
 * without error.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class ZeroBlob extends Tag implements Blob {

    public ZeroBlob() {
    }

    public ZeroBlob(int expectedSize) {
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.empty();
    }

    @Override
    public void map(Supplier<ByteBuffer> open,
                    Function<ByteBuffer, ByteBuffer> function,
                    Supplier<ByteBuffer> close) {
    }

    @Override
    public long size() {
        return 0;
    }

    public class ZeroReadableBlob implements ReadableBlob {
        @Override
        public void read(OutputStream stream) throws IOException {
        }

        @Override
        public void close() {
        }
    }

    public class ZeroWritableBlob implements WritableBlob {
        @Override
        public void clear() {
        }

        @Override
        public int write(InputStream stream) throws BlobOverflowException {
            throw new BlobOverflowException();
        }

        @Override
        public int write(InputStream stream, int size) throws BlobOverflowException {
            throw new BlobOverflowException();
        }

        @Override
        public int write(byte b) throws BlobOverflowException {
            throw new BlobOverflowException();
        }

        @Override
        public int write(byte[] a) throws BlobOverflowException {
            throw new BlobOverflowException();
        }

        @Override
        public int write(ByteBuffer buffer) throws BlobOverflowException {
            throw new BlobOverflowException();
        }

        @Override
        public void close() {

        }
    }

    @Override
    public WritableBlob getWritableBlob() {
        return new ZeroWritableBlob();
    }

    @Override
    public boolean isFileBlob() {
        return false;
    }

    @Override
    public String getFilePath() throws NotFileBlob {
        throw new NotFileBlob();
    }

    @Override
    public Completable moveToFile(String path) {
        return Completable.complete();
    }
}
