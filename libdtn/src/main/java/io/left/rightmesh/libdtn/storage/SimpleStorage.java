package io.left.rightmesh.libdtn.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.rxparser.RxParserException;
import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.data.MetaBundle;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Parser;
import io.left.rightmesh.libdtn.data.bundleV7.BundleV7Serializer;
import io.left.rightmesh.libdtn.events.BundleIndexed;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * SimpleStorage stores bundle in files but keep an index in memory of all the bundles in storage.
 * Each entry in the index contains a filesystem path to the bundle as well as a "MetaBundle" that
 * holds some information about the bundle that can be used for routing without having to pull the
 * bundle from storage until the very last moment.
 *
 * <p>If the payload of the Bundle is already store in a FileBLOB, the index will keep a reference
 * to it and will not serialize it within the bundle file. By so doing, a payload FileBLOB need not
 * be copied multiple time.
 *
 * <p>The SimpleStorage is configurable through {@see DTNConfiguration} by updating two values:
 * <ul>
 *     <li>COMPONENT_ENABLE_SIMPLE_STORAGE: enable/disable SimpleStorage</li>
 *     <li>SIMPLE_STORAGE_PATH: update the list of path to be used as storage.
 *                              Storage priority follows the list order</li>
 * </ul>
 *
 * @author Lucien Loiseau on 20/09/18.
 */
public class SimpleStorage extends Component implements BundleStorage {

    public static final String BLOB_FOLDER = File.separator + "blob" + File.separator;
    public static final String BUNDLE_FOLDER = File.separator + "bundle" + File.separator;

    // ---- SINGLETON ----
    private static SimpleStorage instance = new SimpleStorage();
    public static SimpleStorage getInstance() { return instance;  }
    public static void init() {    }

    private static class IndexEntry extends MetaBundle {
        String path; /* path to the file where the bundle is serialized */
        boolean has_blob;
        String blob_path;

        IndexEntry(Bundle bundle) {
            super(bundle);
        }

        IndexEntry(MetaBundle meta, String path) {
            super(meta);
            this.path = path;
            has_blob = false;
            blob_path = "";
        }

        IndexEntry(MetaBundle meta, String path, boolean has_blob, String blobPath) {
            super(meta);
            this.path = path;
            this.has_blob = has_blob;
            blob_path = blobPath;
        }
    }
    private LinkedList<String> storage_paths = new LinkedList<>();
    private Map<BundleID, IndexEntry> index = Collections.synchronizedMap(new HashMap<>());

    private SimpleStorage() {
        super(DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE);
        DTNConfiguration.<Set<String>>get(DTNConfiguration.Entry.SIMPLE_STORAGE_PATH).observe()
                .subscribe(
                        updated_paths -> {
                            /* remove obsolete path */
                            LinkedList<String> pathsToRemove = new LinkedList<>();
                            storage_paths.stream()
                                    .filter(p -> !updated_paths.contains(p))
                                    .map(pathsToRemove::add)
                                    .count();
                            pathsToRemove.stream().map(this::removePath).count();

                            /* add new path */
                            LinkedList<String> pathsToAdd = new LinkedList<>();
                            updated_paths.stream()
                                    .filter(p -> !storage_paths.contains(p))
                                    .map(pathsToAdd::add)
                                    .count();
                            pathsToAdd.stream().map(this::addPath).count();
                        }
                );
    }

    private boolean removePath(String path) {
        if (storage_paths.contains(path)) {
            Set<BundleID> bundlesToRemove = new HashSet<>();
            index.keySet().stream()
                    .filter(b -> index.get(b).path.startsWith(path))
                    .map(b -> {bundlesToRemove.add(b); return b;})
                    .count();
            bundlesToRemove.stream().map(index::remove).count();
            storage_paths.remove(path);
            return true;
        }
        return false;
    }

    private boolean addPath(String path) {
        if (!storage_paths.contains(path)) {
            File f = new File(path);
            if (f.exists() && f.canRead() && f.canWrite()) {
                File fblob = new File(path + BLOB_FOLDER);
                File fbundle = new File(path + BUNDLE_FOLDER);
                if (!fblob.exists() && !fblob.mkdir()) {
                    return false;
                }
                if (!fbundle.exists() && !fbundle.mkdir()) {
                    return false;
                }
                indexBundles(fbundle);
                storage_paths.add(path);
                return true;
            }
        }
        return false;
    }

    private void indexBundles(File folder) {
        for (final File file : folder.listFiles()) {
            /*
             * preparing the parser. We just parse the file header and the primary block of
             * the bundle and then build a MetaBundle that will be use for processing
             */
            CborParser parser = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (p, ___, item) -> {
                                p.save("header", item);
                            })
                    .cbor_open_array((__, ___, ____) -> {})
                    .cbor_parse_custom_item(
                            BundleV7Parser.PrimaryBlockItem::new,
                            (p, ___, item) -> {
                                MetaBundle meta = new MetaBundle(item.b);
                                meta.bundle_size = p.<FileHeaderItem>get("header").bundle_size;
                                index.put(meta.bid, new IndexEntry(meta,
                                        file.getAbsolutePath(),
                                        p.<FileHeaderItem>get("header").has_blob,
                                        p.<FileHeaderItem>get("header").blob_path));
                                RxBus.post(new BundleIndexed(meta));
                            });
            ByteBuffer buffer = ByteBuffer.allocate(500);
            FileChannel in;
            try {
                in = new FileInputStream(file).getChannel();
            } catch(FileNotFoundException fnfe) {
                continue; /* cannot happen */
            }

            /* extracting meta */
            boolean done = false;
            try {
                while ((in.read(buffer) > 0) && !done) {
                    buffer.flip();
                    done = parser.read(buffer);
                    buffer.clear();
                }
                in.close();
            } catch(RxParserException | IOException rpe) {
                continue;
            }
        }
    }

    private static File createNewFile(String prefix, String suffix, String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return File.createTempFile(prefix, suffix, f);
        } else {
            return null;
        }
    }

    private static long spaceLeft(String path) {
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
     * @throws StorageFullException if there isn't enough space in SimpleStorage
     */
    static FileBLOB createBLOB(long expectedSize) throws StorageUnavailableException, StorageFullException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        for (String path : getInstance().storage_paths) {
            if (spaceLeft(path + BLOB_FOLDER) > expectedSize) {
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
                    String safeBID = bid.toString().replaceAll("/", "_");
                    File fbundle = createNewFile("bundle-", ".bundle", path + BUNDLE_FOLDER);
                    if (fbundle != null) {
                        return fbundle;
                    }
                } catch (IOException io) {
                    System.out.println("IOException createNewFile: " + io.getMessage() + " : " + path + BUNDLE_FOLDER + "bid-" + bid + ".bundle");
                }
            }
        }
        throw new StorageFullException();
    }

    /**
     * count the number of bundles accessible
     *
     * @return number of bundles indexed
     */
    public static int count() throws StorageUnavailableException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        return getInstance().index.size();
    }

    /**
     * store a bundle into persistent storage. This operation can take time so it is done in
     * a different thread and returns a Completable.
     *
     * @param bundle to store
     * @return Completable
     */
    public static Single<Bundle> store(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.<Bundle>create(
                s -> {
                    if (!getInstance().index.containsKey(bundle.bid)) {
                        /* prepare bundle encoder and metabundle */
                        CborEncoder bundleEncoder = BundleV7Serializer.encode(bundle);
                        final MetaBundle meta = new MetaBundle(bundle, bundleEncoder);

                        /* create file and assess available space */
                        File fbundle;
                        try {
                            fbundle = createBundleEntry(bundle.bid, meta.bundle_size);
                        } catch (StorageFullException sfe) {
                            s.onError(new Throwable("storage is full"));
                            return;
                        }
                        if (fbundle == null) {
                            s.onError(new Throwable("could not create file"));
                            return;
                        }

                        /* prepare bundle: we do not serialize the payload fileBLOB, if any */
                        boolean has_blob = false;
                        String  blob_path = "";
                        BLOB blob = new NullBLOB();
                        if(bundle.getPayloadBlock().data instanceof FileBLOB) {
                            blob = bundle.getPayloadBlock().data;
                            has_blob = true;
                            blob_path = ((FileBLOB)blob).getAbsolutePath();

                            // momentarily remove the blob from bundle for serialization
                            bundle.getPayloadBlock().data = new NullBLOB();
                        }
                        final IndexEntry entry = new IndexEntry(
                                meta,
                                fbundle.getAbsolutePath(),
                                has_blob,
                                blob_path);

                        /*
                         * the bundle will be serialized in the file as a CBOR array containing
                         * two item, the file header and the bundle
                         */
                        CborEncoder enc = CBOR.encoder()
                                .cbor_start_array(2)  /* File = header + bundle */
                                .cbor_start_array(3)  /* File Header */
                                .cbor_encode_int(meta.bundle_size)
                                .cbor_encode_boolean(has_blob)
                                .cbor_encode_text_string(blob_path)
                                .merge(bundleEncoder); /* bundle */

                        /* bundle and actual serialization */
                        OutputStream out = new BufferedOutputStream(
                                new FileOutputStream(fbundle));

                        enc.observe().subscribe( /* same thread */
                                buffer -> {
                                    while (buffer.hasRemaining()) {
                                        out.write(buffer.get());
                                    }
                                    getInstance().index.put(bundle.bid, entry);
                                    s.onSuccess(meta);
                                },
                                e -> {
                                    fbundle.delete();
                                    s.onError(new Throwable("serialized into file failed"));
                                });
                        out.close();

                        /* if we removed the payload BLOB, we put it back */
                        if(has_blob) {
                            bundle.getPayloadBlock().data = blob;
                        }

                    }
                }
        ).subscribeOn(Schedulers.io());
    }

    /**
     * returns true if the bundle id is present in the index
     *
     * @param id of the bundle
     * @return true if present, false otherwise
     * @throws StorageUnavailableException if the SimpleStorage is disabled
     */
    public static boolean contains(BundleID id) throws StorageUnavailableException {
        if (!getInstance().isEnabled()) {
            throw new StorageUnavailableException();
        }

        return getInstance().index.containsKey(id);
    }

    public static Single<MetaBundle> getMetaBundle(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.create(s -> {
            if(contains(id)) {
                s.onSuccess(getInstance().index.get(id));
            } else {
                s.onError(new BundleNotFoundException());
            }
        });
    }

    /**
     * Pull a bundle from storage. This operation can take some time so it is done in a different
     * thread and returns a Single RxJava object.
     *
     * @param id of the bundle
     * @return Single completes with the bundle on success, throw an error otherwise
     */
    public static Single<Bundle> get(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        return Single.<Bundle>create(s -> {
            if (!getInstance().index.containsKey(id)) {
                s.onError(new Throwable("no such bundle in storage: " + id.toString()));
                return;
            }

            /* pulling entry from index */
            IndexEntry entry = getInstance().index.get(id);
            File fbundle = new File(entry.path);
            if (!fbundle.exists() || !fbundle.canRead()) {
                s.onError(new Throwable("can't read bundle file in storage: " + entry.path));
                return;
            }

            /* preparing file and parser */
            CborParser parser = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (p, ___, item) -> p.save("header", item))
                    .cbor_parse_custom_item(
                            BundleV7Parser.BundleItem::new,
                            (p, ___, item) -> {
                                if(p.<FileHeaderItem>get("header").has_blob) {
                                    String path = p.<FileHeaderItem>get("header").blob_path;
                                    try {
                                        item.bundle.getPayloadBlock().data = new FileBLOB(path);
                                        s.onSuccess(item.bundle);
                                    } catch(IOException io) {
                                        s.onError(new Throwable("can't retrieve payload blob"));
                                    }
                                }
                                s.onSuccess(item.bundle);
                            });

            /* extracting bundle from file */
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            FileChannel in = new FileInputStream(entry.path).getChannel();
            try {
                boolean done = false;
                while ((in.read(buffer) > 0) && !done) {
                    buffer.flip();
                    done = parser.read(buffer);
                    buffer.clear();
                }
                in.close();
            } catch(RxParserException | IOException rpe) {
                /* should not happen */
                s.onError(rpe);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Remove a bundle from persistent storage. Removing implies deleting the file, deleting
     * the payload blob (if any) and remove the reference from the index. This operation can take
     * some time so it is done in a different thread and returns a Comnpletable.
     *
     * @param id of the bundle to delete
     * @return Completable
     */
    public static Completable remove(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        return Completable.create(s -> {
            if (!getInstance().index.containsKey(id)) {
                s.onComplete();
                return;
            }
            String error = "";
            IndexEntry meta = getInstance().index.get(id);
            getInstance().index.remove(id);

            File fbundle = new File(meta.path);
            if (fbundle.exists() && !fbundle.canWrite()) {
                error += "can't access bundle file for deletion";
            } else {
                fbundle.delete();
            }

            if (meta.has_blob) {
                File fblob = new File(meta.blob_path);
                if (fblob.exists() && !fbundle.canWrite()) {
                    error += "can't access payload blob file for deletion";
                } else {
                    fblob.delete();
                }
            }

            if(error.length() > 0) {
                s.onError(new Throwable(error));
            } else {
                s.onComplete();
            }
        });
    }

    /**
     * Clear the entire persistent storage. Delete all bundles files and related blob (if any) and
     * clear the index.
     *
     * @return
     */
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

    private static class FileHeaderItem implements CborParser.ParseableItem {

        long    bundle_size;
        boolean has_blob;
        String  blob_path;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(3)
                    .cbor_parse_int((__, ___, i) -> bundle_size = i)
                    .cbor_parse_boolean((__,b) -> has_blob = b)
                    .cbor_parse_text_string_full((__, str) -> blob_path = str);
        }
    }
}
