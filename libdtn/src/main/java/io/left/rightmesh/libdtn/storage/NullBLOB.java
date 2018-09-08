package io.left.rightmesh.libdtn.storage;

import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * NullBlob is a BLOB of size zero and that contains no data.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class NullBLOB extends BLOB {

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.empty();
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ReadableBLOB() {
            @Override
            public void read(OutputStream stream) throws IOException {
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new WritableBLOB() {
            @Override
            public void clear() {
            }

            @Override
            public int write(InputStream stream, int size) throws IOException, BLOBOverflowException {
                return 0;
            }

            @Override
            public int write(byte b) throws IOException, BLOBOverflowException {
                return 0;
            }

            @Override
            public int write(byte[] a) throws IOException, BLOBOverflowException {
                return 0;
            }

            @Override
            public void close() {

            }
        };
    }
}
