package io.left.rightmesh.libdtn.storage;

import io.left.rightmesh.libdtn.bundleV6.Bundle;
import io.left.rightmesh.libdtn.bundleV6.BundleID;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * VolatileStorage holds all the Bundle in memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class VolatileStorage implements BundleStorage {

    //todo makes it configurable
    private static final int VOLATILE_BLOB_STORAGE_MAX_CAPACITY = 10000000; // 10M
    private static int CurrentBLOBMemoryUsage = 0;

    /**
     * Create a new {@see VolatileBLOB}.
     *
     * @param expectedSize of the BLOB
     * @return a new VolatileBLOB with capacity of expectedSize
     * @throws StorageFullException if there isn't enough space in Volatile Memory
     */
    public static VolatileBLOB createBLOB(int expectedSize) throws StorageFullException {
        if (expectedSize > (VOLATILE_BLOB_STORAGE_MAX_CAPACITY - CurrentBLOBMemoryUsage)) {
            throw new StorageFullException();
        }
        return new VolatileBLOB(expectedSize);
    }

    /**
     * VolatileBLOB holds a {@see BLOB} in volatile memory. It is implemented with a Byte array.
     * Useful if the BLOB is small or if performance are required. Data holds by VolatileBLOB are
     * lost in case of reboot.
     *
     * @author Lucien Loiseau on 26/07/18.
     */
    public static class VolatileBLOB extends BLOB {

        /** the actual buffer. */
        private byte[] data;

        /** represents the current cursor in writing mode, or the limit in reading mode. */
        private int position;

        private enum BLOBMode {
            CLOSED,
            LOCKED_READ,
            LOCKED_WRITE
        }

        /**
         * Constructor: initialize the byte array to be of size expectedSize.
         *
         * @param expectedSize of the byte array
         */
        public VolatileBLOB(int expectedSize) {
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
            synchronized (super.lock) {
                return position;
            }
        }

        @Override
        public Flowable<ByteBuffer> observe() {
            return Flowable.generate(
                    () -> {
                        try {
                            switchBLOBMode(BLOB.BLOBMode.LOCKED_READ);
                        } catch (BLOBStateException be) {
                            return null;
                        }

                        return new MutablePair<>(
                                0,
                                ByteBuffer.allocate(2048));
                    },
                    (state, emitter) -> {
                        if (state == null) {
                            emitter.onError(new Throwable("couldn't lock the BLOB"));
                            return state;
                        }

                        Integer pos = state.left;
                        ByteBuffer buffer = state.right;
                        if (pos == position) {
                            emitter.onComplete();
                            switchBLOBMode(BLOB.BLOBMode.CLOSED);
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
        protected InnerReadableBLOB readableBLOBFactory() throws BLOBStateException {
            return new ReadableVolatileBLOB();
        }

        @Override
        protected InnerWritableBLOB writableBLOBFactory() throws BLOBStateException {
            return new WritableVolatileBLOB();
        }

        public class ReadableVolatileBLOB extends InnerReadableBLOB {

            ReadableVolatileBLOB() throws BLOBStateException {
                super();
            }

            @Override
            public void read(OutputStream stream) throws IOException {
                stream.write(data);
            }
        }

        public class WritableVolatileBLOB extends InnerWritableBLOB {

            WritableVolatileBLOB() throws BLOBStateException {
                super();
            }

            @Override
            public void clear() {
                data = new byte[data.length];
                position = 0;
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
        }
    }

    private Map<BundleID, Bundle> bundles = new HashMap<>();

    @Override
    public Completable clear() {
        return Completable.create(s -> {
            bundles.clear();
            s.onComplete();
        });
    }

    @Override
    public Single<Integer> count() {
        return Single.create(s -> {
            s.onSuccess(bundles.size());
        });
    }

    @Override
    public Completable store(Bundle bundle) {
        return Completable.create(s -> {
            BundleID bid = new BundleID(bundle);

            this.contains(bid).subscribe(
                    b -> {
                        if (b) {
                            s.onError(new BundleAlreadyExistsException());
                        } else {
                            bundles.put(bid, bundle);
                            s.onComplete();
                        }
                    });
        });
    }

    @Override
    public Single<Boolean> contains(BundleID id) {
        return Single.create(s -> s.onSuccess(bundles.containsKey(id)));
    }

    @Override
    public Single<Bundle> get(BundleID id) {
        return Single.create(s -> {
            this.contains(id).subscribe(
                    b -> {
                        if (!b) {
                            s.onError(new BundleNotFoundException());
                        } else {
                            s.onSuccess(bundles.get(id));
                        }
                    });
        });
    }

    @Override
    public Completable remove(BundleID id) {
        return Completable.create(s -> {
            this.contains(id).subscribe(
                    b -> {
                        if (!b) {
                            s.onComplete();
                        } else {
                            bundles.remove(id);
                            s.onComplete();
                        }
                    });
        });
    }
}
