package io.left.rightmesh.libdtn.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
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
public class SimpleStorage extends Component implements BundleStorage {

    public static final String BLOB_FOLDER = File.separator+"blob"+File.separator;
    public static final String BUNDLE_FOLDER = File.separator+"bundle"+File.separator;

    // ---- SINGLETON ----
    private static SimpleStorage instance = new SimpleStorage();

    public static SimpleStorage getInstance() {
        return instance;
    }

    public static void init() {
    }

    private LinkedList<String> storage_paths = new LinkedList<>();
    private HashMap<BundleID, String> index = new HashMap<>();

    private SimpleStorage() {
        super(DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE);
        DTNConfiguration.<Set<String>>get(DTNConfiguration.Entry.SIMPLE_STORAGE_PATH).observe()
                .subscribe(
                        paths -> {
                            storage_paths.clear();
                            for (String path : paths) {
                                addPath(path);
                            }
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
                indexBundles(f);
                storage_paths.add(path);
            }
        }
    }

    private void indexBundles(File directory) {
        String[] indexFileNames = directory.list((__, name) -> name.endsWith(".bundle"));
        if (indexFileNames != null) {
            for (String name : indexFileNames) {

            }
        }
    }

    public static File createNewFile(String prefix, String suffix, String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return File.createTempFile(prefix, suffix, f);
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
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        for (String path : getInstance().storage_paths) {
            if (spaceLeft(path + BLOB_FOLDER) < expectedSize) {
                continue;
            } else {
                try {
                    File fblob = createNewFile("blob-", ".blob", path + BLOB_FOLDER);
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

    private static File createBundleEntry(BundleID bid, long expectedSize) throws StorageFullException {
        for (String path : getInstance().storage_paths) {
            if (spaceLeft(path + BUNDLE_FOLDER) > expectedSize) {
                try {
                    String safeBID=bid.toString().replaceAll("/", "_");
                    File fbundle = new File(path + BUNDLE_FOLDER + "bid-" + safeBID + ".bundle");
                    if (fbundle.createNewFile()) {
                        return fbundle;
                    }
                } catch (IOException io) {
                    System.out.println("IOException createNewFile: "+io.getMessage()+" : "+path + BUNDLE_FOLDER + "bid-" + bid + ".bundle");
                }
            }
        }
        throw new StorageFullException();
    }

    public static Single<Integer> count() {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.just(getInstance().index.size());
    }

    public static Completable store(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(
                s -> {
                    if (!getInstance().index.containsKey(bundle.bid)) {
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
                            fbundle = createBundleEntry(bundle.bid, size[0]);
                        } catch (StorageFullException sfe) {
                            s.onError(new Throwable("storage is full"));
                            return;
                        }
                        if (fbundle == null) {
                            s.onError(new Throwable("could not create file"));
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
                            getInstance().index.put(bundle.bid, fbundle.getAbsolutePath());
                            s.onComplete();
                        } else {
                            fbundle.delete();
                            s.onError(new Throwable("serialized into file failed"));
                        }

                    }
                }
        ).subscribeOn(Schedulers.io());
    }

    public static Single<Boolean> contains(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.just(getInstance().index.containsKey(id));
    }

    public static Single<Bundle> get(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.<Bundle>create(s -> {
            if (!getInstance().index.containsKey(id)) {
                s.onError(new Throwable("no such bundle in storage: " + id.toString()));
                return;
            }
            String path = getInstance().index.get(id);
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

    public static Completable remove(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            if (!getInstance().index.containsKey(id)) {
                s.onComplete();
                return;
            }
            String path = getInstance().index.get(id);
            File fbundle = new File(path);
            if (!fbundle.exists() || !fbundle.canWrite()) {
                s.onError(new Throwable("can't access bundle file for deletion"));
                return;
            }
            fbundle.delete();
            getInstance().index.remove(id);
            s.onComplete();
        });
    }

    public static Completable clear() {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            Set<BundleID> bids = new HashSet<>();
            bids.addAll(getInstance().index.keySet());
            for (BundleID id : bids) {
                remove(id).subscribe();
            }
            s.onComplete();
        });
    }
}
