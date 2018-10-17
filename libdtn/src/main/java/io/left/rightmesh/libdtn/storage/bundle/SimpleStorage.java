package io.left.rightmesh.libdtn.storage.bundle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
import io.left.rightmesh.libdtn.storage.blob.BLOB;
import io.left.rightmesh.libdtn.storage.blob.FileBLOB;
import io.left.rightmesh.libdtn.storage.blob.NullBLOB;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Observable;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SIMPLE_STORAGE;

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
 * <li>COMPONENT_ENABLE_SIMPLE_STORAGE: enable/disable SimpleStorage</li>
 * <li>SIMPLE_STORAGE_PATH: update the list of path to be used as storage.
 * StorageAPI priority follows the list order</li>
 * </ul>
 *
 * @author Lucien Loiseau on 20/09/18.
 */
public class SimpleStorage extends Component implements BundleStorage {

    private static final String TAG = "SimpleStorage";

    public static final String BLOB_FOLDER = File.separator + "blob" + File.separator;
    public static final String BUNDLE_FOLDER = File.separator + "bundle" + File.separator;

    // ---- SINGLETON ----
    private static SimpleStorage instance;
    public static SimpleStorage getInstance() {
        return instance;
    }

    static {
        instance = new SimpleStorage();
        instance.initComponent(COMPONENT_ENABLE_SIMPLE_STORAGE);
        DTNConfiguration.<Set<String>>get(DTNConfiguration.Entry.SIMPLE_STORAGE_PATH).observe()
                .subscribe(
                        updated_paths -> {
                            /* remove obsolete path */
                            LinkedList<String> pathsToRemove = new LinkedList<>();
                            getInstance().storage_paths.stream()
                                    .filter(p -> !updated_paths.contains(p))
                                    .map(pathsToRemove::add)
                                    .count();
                            pathsToRemove.stream().map(instance::removePath).count();

                            /* add new path */
                            LinkedList<String> pathsToAdd = new LinkedList<>();
                            updated_paths.stream()
                                    .filter(p -> !getInstance().storage_paths.contains(p))
                                    .map(pathsToAdd::add)
                                    .count();
                            pathsToAdd.stream().map(instance::addPath).count();
                        }
                );
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    private LinkedList<String> storage_paths = new LinkedList<>();


    /**
     * Count the number of Persistent Bundlel in Storage. This method iterates over the entire index.
     *
     * @return number of Persistent bundle in storage
     */
    public static int count() {
        return (int)Storage.index.values().stream().filter(e -> e.isPersistent).count();
    }

    private boolean removePath(String path) {
        if (storage_paths.contains(path)) {
            Storage.index.forEach(
                    (bid, entry) -> {
                        if(entry.isPersistent && entry.bundle_path.startsWith(path)) {
                            entry.isPersistent = false;
                            if(!entry.isVolatile) {
                                Storage.removeEntry(bid, entry);
                            }
                        }
                    });
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
                indexBundlesFromPath(fbundle);
                storage_paths.add(path);
                return true;
            }
        }
        return false;
    }

    private void indexBundlesFromPath(File folder) {
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
                                p.setReg(0, item);
                            })
                    .cbor_open_array((__, ___, ____) -> {
                    }) /* we are just parsing te primary block */
                    .cbor_parse_custom_item(
                            BundleV7Parser.PrimaryBlockItem::new,
                            (p, ___, item) -> {
                                MetaBundle meta = new MetaBundle(item.b);
                                Storage.IndexEntry entry = Storage.getEntryOrCreate(meta.bid, meta);
                                entry.bundle_path = file.getAbsolutePath();
                                entry.has_blob = p.<FileHeaderItem>getReg(0).has_blob;
                                entry.blob_path = p.<FileHeaderItem>getReg(0).blob_path;
                                entry.isPersistent = true;
                                RxBus.post(new BundleIndexed(meta));
                            });

            ByteBuffer buffer = ByteBuffer.allocate(500);
            FileChannel in;
            try {
                in = new FileInputStream(file).getChannel();
            } catch (FileNotFoundException fnfe) {
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
            } catch (RxParserException | IOException rpe) {
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
    public static FileBLOB createBLOB(long expectedSize) throws StorageUnavailableException, StorageFullException {
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
                    String safeBID = bid.getBIDString().replaceAll("/", "_");
                    File fbundle = createNewFile("bundle-", ".bundle", path + BUNDLE_FOLDER);
                    if (fbundle != null) {
                        return fbundle;
                    }
                } catch (IOException io) {
                    System.out.println("IOException createNewFile: " + io.getMessage() + " : " + path + BUNDLE_FOLDER + "bid=" + bid.getBIDString() + ".bundle");
                }
            }
        }
        throw new StorageFullException();
    }

    /**
     * store a bundle into persistent storage. This operation can take time so it is done in
     * a different thread and returns a Completable.
     *
     * @param bundle to store
     * @return Single of the MetaBundle
     */
    public static Single<Bundle> store(Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        if (Storage.containsPersistent(bundle.bid)) {
            return Single.error(new BundleAlreadyExistsException());
        }

        return Single.<Bundle>create(
                s -> {
                    /* prepare bundle: we do not serialize the payload if it is a fileBLOB */
                    boolean has_blob = false;
                    String blob_path = "";
                    BLOB blob = new NullBLOB();
                    if (bundle.getPayloadBlock().data instanceof FileBLOB) {
                        blob = bundle.getPayloadBlock().data;
                        has_blob = true;
                        blob_path = ((FileBLOB) blob).getAbsolutePath();

                        /* momentarily remove the blob from bundle for serialization */
                        bundle.getPayloadBlock().data = new NullBLOB();
                    }

                    /* prepare bundle encoder and metabundle */
                    CborEncoder bundleEncoder = BundleV7Serializer.encode(bundle);
                    final MetaBundle meta = new MetaBundle(bundle);

                    /*
                     * the bundle will be serialized in the file as a CBOR array containing
                     * two item, the file header and the bundle
                     */
                    CborEncoder enc = CBOR.encoder()
                            .cbor_start_array(2)  /* File = header + bundle */
                            .cbor_start_array(2)  /* File Header */
                            .cbor_encode_boolean(has_blob)
                            .cbor_encode_text_string(blob_path)
                            .merge(bundleEncoder); /* bundle */

                    /* asses file size */
                    AtomicLong size = new AtomicLong();
                    BundleV7Serializer.encode(bundle).observe()
                            .subscribe(
                                    buffer -> size.set(size.get() + buffer.remaining()),
                                    e -> size.set(-1),
                                    () -> { /* ignore */ });

                    /* create file */
                    File fbundle;
                    try {
                        fbundle = createBundleEntry(bundle.bid, size.get());
                    } catch (StorageFullException sfe) {
                        if (has_blob) {
                            bundle.getPayloadBlock().data = blob;
                        }
                        s.onError(new Throwable("storage is full"));
                        return;
                    }

                    /* actual serialization of the bundle */
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(fbundle));
                    enc.observe().toObservable().subscribe( /* same thread */
                            new DisposableObserver<ByteBuffer>() {
                                @Override
                                public void onNext(ByteBuffer buffer) {
                                    try {
                                        while (buffer.hasRemaining()) {
                                            out.write(buffer.get());
                                        }
                                    } catch (IOException io) {
                                        dispose();
                                        closeSilently(out);
                                        meta.tag("serialization_failed");
                                    }
                                }

                                @Override
                                public void onError(Throwable t) {
                                    fbundle.delete();
                                    closeSilently(out);
                                    meta.tag("serialization_failed");
                                }

                                @Override
                                public void onComplete() {
                                    closeSilently(out);
                                }
                            });

                    /**
                     * we put the BLOB back into the data of the bundle. It is technically
                     * unnecessary as any the reference to this Bundle will use the MetaBundle that
                     * we are returning and so the actual bundle reference shall normally be lost
                     * and garbage collected
                     */
                    if (has_blob) {
                        bundle.getPayloadBlock().data = blob;
                    }

                    if(!meta.isTagged("serialization_failed")) {
                        final Storage.IndexEntry entry = Storage.getEntryOrCreate(meta.bid, meta);
                        entry.isPersistent = true;
                        entry.bundle_path = fbundle.getAbsolutePath();
                        entry.has_blob = has_blob;
                        entry.blob_path = blob_path;
                        bundle.tag("in_storage");
                        s.onSuccess(meta);
                    } else {
                        s.onError(new Throwable("bundle failed to serialize into file"));
                    }
                }
        ).subscribeOn(Schedulers.io());
    }

    static void closeSilently(OutputStream s) {
        try {
            s.close();
        } catch(IOException io) {
            /* ignore */
        }
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
            if(!Storage.containsPersistent(id)) {
                s.onError(new Throwable("no such bundle in storage: " + id.getBIDString()));
                return;
            }

            /* pulling entry from index */
            Storage.IndexEntry entry = Storage.index.get(id);
            File fbundle = new File(entry.bundle_path);
            if (!fbundle.exists() || !fbundle.canRead()) {
                s.onError(new Throwable("can't read bundle file in storage: " + entry.bundle_path));
                return;
            }

            /* preparing file and parser */
            CborParser parser = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (p, ___, item) -> p.setReg(0, item))
                    .cbor_parse_custom_item(
                            BundleV7Parser.BundleItem::new,
                            (p, ___, item) -> {
                                if (p.<FileHeaderItem>getReg(0).has_blob) {
                                    String path = p.<FileHeaderItem>getReg(0).blob_path;
                                    try {
                                        item.bundle.getPayloadBlock().data = new FileBLOB(path);
                                    } catch (IOException io) {
                                        throw new RxParserException("can't retrieve payload blob");
                                    }
                                }
                                item.bundle.tag("in_storage");
                                p.setReg(1, item.bundle); // ret value
                            });

            /* extracting bundle from file */
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            FileChannel in = new FileInputStream(entry.bundle_path).getChannel();
            try {
                boolean done = false;
                while ((in.read(buffer) > 0) && !done) { // read buffer from file
                    buffer.flip();
                    done = parser.read(buffer);
                    buffer.clear();
                }
                in.close();
            } catch (RxParserException | IOException rpe) {
                /* should not happen */
                s.onError(rpe);
            }

            Bundle ret = parser.getReg(1);
            parser.reset();
            if(ret != null) {
                s.onSuccess(ret);
            } else {
                s.onError(new Throwable("can't retrieve bundle from file"));
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Remove a bundle from persistent storage. Removing implies deleting the file, deleting
     * the payload blob (if any) and updating the entry in Storage. If the entry has no volatile
     * copy, it deletes the entry from the index. This operation can take some time so it is done
     * in a different thread and returns a Completable.
     *
     * @param id of the bundle to delete
     * @return Completable
     */
    public static Completable remove(BundleID id) {
        if (!getInstance().isEnabled()) {
            return Completable.error(StorageUnavailableException::new);
        }

        return Completable.create(s -> {
            if (!Storage.containsPersistent(id)) {
                s.onError(new BundleNotFoundException());
                return;
            }

            String error = "";
            Storage.IndexEntry entry = Storage.index.get(id);

            File fbundle = new File(entry.bundle_path);
            if (fbundle.exists() && !fbundle.canWrite()) {
                error += "can't access bundle file for deletion";
            } else {
                fbundle.delete();
            }

            if (entry.has_blob) {
                File fblob = new File(entry.blob_path);
                if (fblob.exists() && !fbundle.canWrite()) {
                    error += "can't access payload blob file for deletion";
                } else {
                    fblob.delete();
                }
            }
            entry.has_blob = false;
            entry.bundle_path = "";
            entry.blob_path = "";
            entry.isPersistent = false;

            if(!entry.isVolatile) {
                Storage.removeEntry(id, entry);
            }

            if (error.length() > 0) {
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

        return Observable.fromIterable(Storage.index.keySet())
                .flatMapCompletable(SimpleStorage::remove)
                .onErrorComplete();
    }

    private static class FileHeaderItem implements CborParser.ParseableItem {

        boolean has_blob;
        String blob_path;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_boolean((__, b) -> has_blob = b)
                    .cbor_parse_text_string_full((__, str) -> blob_path = str);
        }
    }
}
