package io.left.rightmesh.libdtn.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class SimpleStorage implements BundleStorage {

    public static final String BLOB_FOLDER = "/blob/";
    public static final String BUNDLE_FOLDER = "/bundle/";

    private static final Object lock = new Object();
    private static SimpleStorage instance = null;
    private static boolean enabled = false;
    public static SimpleStorage getInstance() {
        synchronized (lock) {
            if(instance == null) {
                instance = new SimpleStorage();
            }
            return instance;
        }
    }

    LinkedList<String> storage_paths = new LinkedList<>();
    HashMap<BundleID, String> index = new HashMap<>();

    private SimpleStorage() {
        DTNConfiguration.<Boolean>get(DTNConfiguration.Entry.ENABLE_SIMPLE_STORAGE).observe()
                .subscribe(enabled -> this.enabled = enabled);
        DTNConfiguration.<Set<String>>get(DTNConfiguration.Entry.SIMPLE_STORAGE_PATH).observe()
                .subscribe(
                    paths -> {
                        storage_paths.clear();
                        storage_paths.addAll(paths);
                    }
                );
    }
    
    private void addPath(String path) {
        if (!storage_paths.contains(path)) {
            File f = new File(path);
            if (f.exists() && f.canRead() && f.canWrite()) {
                File fblob = new File(path + BLOB_FOLDER);
                File fbundle = new File(path + BUNDLE_FOLDER);
                if (!fblob.exists() && !fblob.mkdir()) {
                    return;
                }
                if (!fbundle.exists() && !fbundle.mkdir()) {
                    return;
                }
                // index all existing bundles
                storage_paths.add(path);
            }
        }
    }

    public static File createNewFile(String suffix, String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return File.createTempFile("", suffix, f);
        } else {
            return null;
        }
    }

    public static long spaceLeft(String path) {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return f.getUsableSpace();
        } else {
            return 0;
        }
    }

    /**
     * Create a new {@see FileBLOB}.
     *
     * @return a new FileBLOB with capacity of expectedSize
     * @throws StorageFullException if there isn't enough space in Volatile Memory
     */
    public static FileBLOB createBLOB(long expectedSize) throws StorageUnavailableException, StorageFullException {
        if(!enabled) {
            throw new StorageUnavailableException();
        }

        for (String path : getInstance().storage_paths) {
            if (spaceLeft(path + BLOB_FOLDER) < expectedSize) {
                continue;
            } else {
                try {
                    File fblob = createNewFile(".blob", path);
                    if (fblob != null) {
                        return new FileBLOB(fblob);
                    }
                } catch (IOException io) {
                    // ignore and try next path
                }
            }
        }
        throw new StorageFullException();
    }


    private File createBundleEntry(long expectedSize) throws StorageFullException {
        for (String path : storage_paths) {
            if (spaceLeft(path + BUNDLE_FOLDER) < expectedSize) {
                continue;
            } else {
                try {
                    File fblob = createNewFile(".blob", path);
                    if (fblob != null) {
                        return fblob;
                    }
                } catch (IOException io) {
                    // ignore and try next path
                }
            }
        }
        throw new StorageFullException();
    }

    @Override
    public Single<Integer> count() {
        if(!enabled) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.just(index.size());
    }

    @Override
    public Completable store(Bundle bundle) {
        if(!enabled) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(
                s -> {
                    if (!index.containsKey(bundle.bid)) {
                        // first determine size of bundle
                        CborEncoder encodedB = BundleV7Serializer.encode(bundle);
                        long[] size = {0};
                        encodedB.observe().subscribe(
                                buffer -> {
                                    size[0] += buffer.remaining();
                                },
                                e -> {
                                    size[0] = -1;
                                });
                        if (size[0] < 0) {
                            s.onError(new Throwable("can't serialized bundle into file"));
                            return;
                        }

                        File fbundle = null;
                        try {
                            fbundle = createBundleEntry(size[0]);
                        } catch (StorageFullException sfe) {
                            s.onError(new Throwable("storage is full"));
                            return;
                        }
                        if (fbundle == null) {
                            s.onError(new Throwable("storage is full"));
                            return;
                        }

                        OutputStream out = new BufferedOutputStream(
                                new FileOutputStream(fbundle));

                        //serialize into file
                        AtomicBoolean b = new AtomicBoolean(true);
                        encodedB.observe().subscribe(
                                buffer -> {
                                    while (buffer.hasRemaining()) {
                                        out.write(buffer.get());
                                    }
                                },
                                e -> {
                                    b.set(false);
                                });
                        out.close();

                        if (b.get()) {
                            index.put(bundle.bid, fbundle.getAbsolutePath());
                            s.onComplete();
                        } else {
                            fbundle.delete();
                            s.onError(new Throwable("serialized into file failed"));
                        }

                    }
                }
        ).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> contains(BundleID id) {
        if(!enabled) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.just(index.containsKey(id));
    }

    @Override
    public Single<Bundle> get(BundleID id) {
        if(!enabled) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.<Bundle>create(s -> {
            if (!index.containsKey(id)) {
                s.onError(new Throwable("no such bundle in storage: " + id.toString()));
                return;
            }
            String path = index.get(id);
            File fbundle = new File(path);
            if (!fbundle.exists() || !fbundle.canRead()) {
                s.onError(new Throwable("can't read bundle in storage: " + path));
                return;
            }


            AtomicReference<Bundle> ret = new AtomicReference<>();
            CborParser p = BundleV7Parser.create(ret::set);

            ByteBuffer buffer = ByteBuffer.allocate(2048);
            FileChannel in = new FileInputStream(path).getChannel();
            while (in.read(buffer) > 0) {
                buffer.flip();
                p.read(buffer);
                buffer.clear();
            }
            in.close();
            if (ret.get() != null) {
                s.onSuccess(ret.get());
            } else {
                s.onError(new Throwable("can't retrieve bundle, file is probably corrupt"));
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable remove(BundleID id) {
        if(!enabled) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            if (!index.containsKey(id)) {
                s.onComplete();
                return;
            }
            String path = index.get(id);
            File fbundle = new File(path);
            if (!fbundle.exists() || !fbundle.canWrite()) {
                s.onError(new Throwable("can't access bundle file for deletion"));
                return;
            }
            fbundle.delete();
            index.remove(id);
            s.onComplete();
        });
    }

    @Override
    public Completable clear() {
        if(!enabled) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            for (BundleID id : index.keySet()) {
                remove(id).subscribe();
            }
            s.onComplete();
        });
    }
}
