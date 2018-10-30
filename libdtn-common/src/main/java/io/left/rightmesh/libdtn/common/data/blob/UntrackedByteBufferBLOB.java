package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * UntrackedByteBuffer is a simple ByteBuffer BLOB that doesn't "malloc" to {@link VolatileMemory}.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class UntrackedByteBufferBLOB implements BLOB {

    private ByteBuffer data;


    public UntrackedByteBufferBLOB(int expectedSize) {
        this.data = ByteBuffer.allocate(expectedSize);
        this.data.mark();
    }

    public UntrackedByteBufferBLOB(byte[] data) {
        this.data = ByteBuffer.wrap(data);
        this.data.mark();
    }

    public UntrackedByteBufferBLOB(ByteBuffer data) {
        this.data = ByteBuffer.allocate(data.remaining());
        this.data.put(data);
        this.data.position(0);
        this.data.mark();
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
                ByteBuffer dup = data.duplicate();
                dup.reset();
                while(dup.hasRemaining()) {
                    stream.write(data.get());
                }
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
                } catch (BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return 1;
            }

            @Override
            public int write(byte[] a) throws BLOBOverflowException {
                try {
                    data.put(a);
                } catch (BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws BLOBOverflowException {
                int size = buffer.remaining();
                try {
                    data.put(buffer);
                } catch (BufferOverflowException boe) {
                    throw new BLOBOverflowException();
                }
                return size;
            }

            @Override
            public int write(InputStream stream)
                    throws IOException, BLOBOverflowException {
                int read = data.remaining();
                int size = read;
                int b;
                while (read > 0) {
                    if ((b = stream.read()) == -1) {
                        return (size-read);
                    }
                    data.put((byte) b);
                    read--;
                }
                throw new BLOBOverflowException();
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BLOBOverflowException {
                if (size > (data.remaining())) {
                    throw new BLOBOverflowException();
                }
                int read = size;
                while (read > 0) {
                    data.put((byte) stream.read());
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
