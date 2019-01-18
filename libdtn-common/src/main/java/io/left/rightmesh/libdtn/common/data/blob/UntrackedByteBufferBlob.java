package io.left.rightmesh.libdtn.common.data.blob;

import io.left.rightmesh.libdtn.common.utils.Function;
import io.left.rightmesh.libdtn.common.utils.Supplier;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * UntrackedByteBuffer is a simple ByteBuffer Blob that doesn't "malloc" to {@link VolatileMemory}.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class UntrackedByteBufferBlob extends VolatileBlob {

    ByteBuffer data;

    UntrackedByteBufferBlob() {
    }

    /**
     * Constructor creates an UntrackedByteBufferBlob with an expected size.
     * @param expectedSize of the Blob
     */
    public UntrackedByteBufferBlob(int expectedSize) {
        this.data = ByteBuffer.allocate(expectedSize);
        this.data.mark();
    }

    /**
     * Constructor creates an UntrackedByteBufferBlob from a byte array.
     * @param data array holding the buffer.
     */
    public UntrackedByteBufferBlob(byte[] data) {
        this.data = ByteBuffer.wrap(data);
        this.data.mark();
    }

    /**
     * Constructor creates an UntrackedByteBufferBlob from a ByteBuffer.
     * @param data ByteBuffer holding the buffer.
     */
    public UntrackedByteBufferBlob(ByteBuffer data) {
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
    public void map(Supplier<ByteBuffer> open,
                    Function<ByteBuffer, ByteBuffer> function,
                    Supplier<ByteBuffer> close) throws Exception {
        ByteBuffer opened = open.get();
        ByteBuffer mapped = function.apply(data);
        ByteBuffer closed = close.get();
        ByteBuffer ret = ByteBuffer.allocate(opened.remaining()
                + mapped.remaining()
                + closed.remaining());
        ret.put(opened);
        ret.put(mapped);
        ret.put(closed);
        this.data = ret;
        data.position(0);
        data.mark();
    }

    @Override
    public WritableBlob getWritableBlob() {
        return new WritableBlob() {
            @Override
            public void clear() {
                data.clear();
            }

            @Override
            public int write(byte b) throws BlobOverflowException {
                try {
                    data.put(b);
                } catch (BufferOverflowException boe) {
                    throw new BlobOverflowException();
                }
                return 1;
            }

            @Override
            public int write(byte[] a) throws BlobOverflowException {
                try {
                    data.put(a);
                } catch (BufferOverflowException boe) {
                    throw new BlobOverflowException();
                }
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws BlobOverflowException {
                int size = buffer.remaining();
                try {
                    data.put(buffer);
                } catch (BufferOverflowException boe) {
                    throw new BlobOverflowException();
                }
                return size;
            }

            @Override
            public int write(InputStream stream)
                    throws IOException, BlobOverflowException {
                int read = data.remaining();
                int size = read;
                int b;
                while (read > 0) {
                    if ((b = stream.read()) == -1) {
                        return (size - read);
                    }
                    data.put((byte) b);
                    read--;
                }
                throw new BlobOverflowException();
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BlobOverflowException {
                if (size > (data.remaining())) {
                    throw new BlobOverflowException();
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
