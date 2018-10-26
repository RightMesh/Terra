package io.left.rightmesh.libdtn.core.storage.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.ReadableBLOB;
import io.left.rightmesh.libdtn.common.data.blob.WritableBLOB;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.reactivex.Flowable;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY;

/**
 * VolatileBLOB holds a {@see CoreBLOBFactory} in volatile memory. It is implemented with a Byte array.
 * Useful if the CoreBLOBFactory is small or if performance are required. Data holds by VolatileBLOB are
 * lost in case of reboot.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class VolatileBLOB implements BLOB {

    private static final String TAG = "VolatileBLOB";

    //todo makes it configurable
    private static int BLOBMemoryMaxUsage = 0;
    private static int CurrentBLOBMemoryUsage = 0;

    static void setLimitConf(ConfigurationAPI conf) {
        conf.<Integer>get(VOLATILE_BLOB_STORAGE_MAX_CAPACITY)
                .observe().subscribe(i -> BLOBMemoryMaxUsage = i);
    }

    /**
     * Create a new {@see VolatileBLOB}.
     *
     * @param expectedSize of the CoreBLOBFactory
     * @return a new VolatileBLOB with capacity of expectedSize
     * @throws CoreBLOBFactory.BLOBFactoryException if there isn't enough space in Volatile Memory
     */
    public static VolatileBLOB createBLOB(int expectedSize)
            throws CoreBLOBFactory.BLOBFactoryException {
        if (expectedSize > (BLOBMemoryMaxUsage - CurrentBLOBMemoryUsage)) {
            throw new CoreBLOBFactory.BLOBFactoryException();
        }

        return new VolatileBLOB(expectedSize);
    }

    /**
     * the actual buffer.
     */
    private byte[] data;

    /**
     * represents the current cursor in writing mode, or the limit in reading mode.
     */
    private int position;

    /**
     * Constructor: initialize the byte array to be of size expectedSize.
     *
     * @param expectedSize of the byte array
     */
    VolatileBLOB(int expectedSize) {
        this.data = new byte[expectedSize];
        position = 0;
        CurrentBLOBMemoryUsage += expectedSize;
    }

    /**
     * Constructor: creates a VolatileBLOB out of an array.
     *
     * @param data array to initialize the blob
     */
    public VolatileBLOB(byte[] data) {
        this.data = data;
        position = data.length;
    }

    // CHECKSTYLE IGNORE NoFinalizer
    @Override
    protected void finalize() throws Throwable {
        CurrentBLOBMemoryUsage -= data.length;
        super.finalize();
    }
    // CHECKSTYLE END IGNORE NoFinalizer


    @Override
    public long size() {
        return position;
    }

    private static class State {
        int left;
        ByteBuffer right;
        State(int left, ByteBuffer right) {
            this.left = left;
            this.right = right;
        }
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return Flowable.generate(
                () -> new State(0, ByteBuffer.allocate(2048)),
                (state, emitter) -> {
                    if (state == null) {
                        emitter.onError(new Throwable("couldn't lock the CoreBLOBFactory"));
                        return state;
                    }

                    Integer pos = state.left;
                    ByteBuffer buffer = state.right;
                    if (pos == position) {
                        emitter.onComplete();
                        return state;
                    }
                    buffer.clear();
                    buffer.put(data, pos, Math.min(2048, position - pos));
                    state.left = Math.min(2048, position - pos);
                    buffer.flip();
                    emitter.onNext(buffer);
                    return state;
                });
    }


    @Override
    public ReadableBLOB getReadableBLOB() {
        return new ReadableBLOB() {
            @Override
            public void read(OutputStream stream) throws IOException {
                stream.write(data);
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
                data = new byte[data.length];
                position = 0;
            }

            @Override
            public int write(byte b) throws BLOBOverflowException {
                if (1 > data.length - position) {
                    throw new BLOBOverflowException();
                }
                data[position++] = b;
                return 1;
            }

            @Override
            public int write(byte[] a) throws BLOBOverflowException {
                if (a.length > data.length - position) {
                    throw new BLOBOverflowException();
                }
                System.arraycopy(a, 0, data, position, a.length);
                position += a.length;
                return a.length;
            }

            @Override
            public int write(ByteBuffer buffer) throws BLOBOverflowException {
                int length = buffer.remaining();
                if (length > data.length - position) {
                    throw new BLOBOverflowException();
                }
                while (buffer.hasRemaining()) {
                    data[position++] = buffer.get();
                }
                return length;
            }

            @Override
            public int write(InputStream stream, int size)
                    throws IOException, BLOBOverflowException {
                if (size > (data.length - position)) {
                    throw new BLOBOverflowException();
                }
                position += size;
                return stream.read(data, position, size);
            }

            @Override
            public void close() {
                // do nothing
            }
        };
    }
}