package io.left.rightmesh.libdtn.storage;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.MetaBundle;
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

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_VOLATILE_STORAGE;

/**
 * VolatileStorage holds all the Bundle in memory.
 *
 * @author Lucien Loiseau on 26/07/18.
 */
public class VolatileStorage extends Component implements BundleStorage {

    //todo makes it configurable
    private static final int VOLATILE_BLOB_STORAGE_MAX_CAPACITY = 10000000; // 10M
    private static int CurrentBLOBMemoryUsage = 0;

    // ---- SINGLETON ----
    private static VolatileStorage instance = new VolatileStorage();
    public static VolatileStorage getInstance() { return instance; }
    public static void init() {
    }
    private VolatileStorage() {
        super(COMPONENT_ENABLE_VOLATILE_STORAGE);
    }

    /**
     * Create a new {@see VolatileBLOB}.
     *
     * @param expectedSize of the BLOB
     * @return a new VolatileBLOB with capacity of expectedSize
     * @throws StorageFullException if there isn't enough space in Volatile Memory
     */
    public static VolatileBLOB createBLOB(int expectedSize) throws StorageFullException, StorageUnavailableException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

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
            return position;
        }

        @Override
        public Flowable<ByteBuffer> observe() {
            return Flowable.generate(
                    () -> new MutablePair<>(0, ByteBuffer.allocate(2048)),
                    (state, emitter) -> {
                        if (state == null) {
                            emitter.onError(new Throwable("couldn't lock the BLOB"));
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

    private static class IndexEntry extends MetaBundle {
        Bundle bundle;
        IndexEntry(Bundle bundle) {
            super(bundle);
            this.bundle = bundle;
        }
    }

    private Map<BundleID, IndexEntry> bundles = new HashMap<>();

    public static Completable clear() {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            getInstance().bundles.clear();
            s.onComplete();
        });
    }

    public static Single<Integer> count() {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.create(s -> {
            s.onSuccess(getInstance().bundles.size());
        });
    }

    public static Completable store(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            contains(bundle.bid).subscribe(
                    b -> {
                        if (b) {
                            s.onError(new BundleAlreadyExistsException());
                        } else {
                            IndexEntry meta = new IndexEntry(bundle);
                            getInstance().bundles.put(bundle.bid, meta);
                            s.onComplete();
                        }
                    });
        });
    }

    public static Single<Boolean> contains(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.create(s -> s.onSuccess(getInstance().bundles.containsKey(id)));
    }

    public static Single<Bundle> get(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return contains(id).flatMap(b ->
                b ? Single.just(getInstance().bundles.get(id).bundle) : Single.error(BundleNotFoundException::new)
        );
    }

    public static Completable remove(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            contains(id).subscribe(
                    b -> {
                        if (!b) {
                            s.onComplete();
                        } else {
                            getInstance().bundles.remove(id);
                            s.onComplete();
                        }
                    });
        });
    }
}
