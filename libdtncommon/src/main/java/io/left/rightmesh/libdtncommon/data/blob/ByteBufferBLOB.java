package io.left.rightmesh.libdtncommon.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class ByteBufferBLOB implements BLOB {

    /**
     * the actual buffer.
     */
    private ByteBuffer data;

    /**
     * Constructor: initialize the byte array to be of size expectedSize.
     *
     * @param expectedSize of the byte array
     */
    public ByteBufferBLOB(int expectedSize) {
        this.data = ByteBuffer.allocate(expectedSize);
    }

    /**
     * Constructor: creates a VolatileBLOB out of an array.
     *
     * @param data array to initialize the blob
     */
    public ByteBufferBLOB(byte[] data) {
        this.data = ByteBuffer.wrap(data);
        this.data.mark();
    }

    public ByteBufferBLOB(ByteBuffer data) {
        this.data = ByteBuffer.allocate(data.remaining());
        this.data.put(data);
        this.data.position(0);
    }

    @Override
    public long size() {
        return data.limit();
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.create(s -> {
            ByteBuffer dup = data.duplicate();
            dup.reset();
            s.onNext(dup);
            s.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ReadableBLOB() {
            @Override
            public void read(OutputStream stream) throws IOException {
                stream.write(data.array());
            }

            @Override
            public void close() {
                // do nothing
            }
        };
    }

    @Override
    public WritableBLOB getWritableBLOB() {
        return new WritableBLOB() {
            @Override
            public void clear() {
                data.clear();
            }

            @Override
            public int write(byte b) throws BLOBOverflowException {
                try {
                    data.put(b);
                } catch(BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return 1;
            }

            @Override
            public int write(byte[] a) throws BLOBOverflowException {
                try {
                    data.put(a);
                } catch(BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws BLOBOverflowException {
                int size = buffer.remaining();
                try {
                    data.put(buffer);
                } catch(BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return size;
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BLOBOverflowException {
                if (size > (data.remaining())) {
                    throw new BLOBOverflowException();
                }
                int read = size;
                while(read > 0) {
                    data.put((byte)stream.read());
                    read--;
                }
                return size;
            }

            @Override
            public void close() {
                data.flip();
                data.mark();
            }
        };
    }
}
