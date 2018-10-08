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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
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

        IndexEntry(MetaBundle meta, String path, String blobPath) {
            super(meta);
            this.path = path;
            has_blob = true;
            blob_path = blobPath;
        }
    }

    private LinkedList<String> storage_paths = new LinkedList<>();
    private Map<BundleID, IndexEntry> index = Collections.synchronizedMap(new HashMap<>());

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
                indexBundles(fbundle);
                storage_paths.add(path);
            }
        }
    }

    private void indexBundles(File folder) {
        for (final File file : folder.listFiles()) {
            /* preparing file and parser, we just parse the file header */
            CborParser p = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (__, ___, item) -> {
                                if(item.has_blob) {
                                    index.put(item.meta.bid, new IndexEntry(item.meta,
                                            file.getAbsolutePath(), item.blob_path));
                                } else {
                                    index.put(item.meta.bid, new IndexEntry(item.meta,
                                            file.getAbsolutePath()));
                                }
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
                    done = p.read(buffer);
                    buffer.clear();
                }
                in.close();
            } catch(RxParserException | IOException rpe) {
                continue;
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
                        /* prepare bundle encoder and metabundle */
                        CborEncoder bundleEncoder = BundleV7Serializer.encode(bundle);
                        MetaBundle meta = new MetaBundle(bundle, bundleEncoder);

                        /* create file and assess available space */
                        File fbundle = null;
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

                        /*
                         * the bundle will be serialized in the file as a CBOR array containing
                         * two item, the file header and the bundle
                         */
                        CborEncoder enc = CBOR.encoder();
                        if(has_blob) {
                            enc.cbor_start_array(2)
                                    .cbor_start_array(3)
                                    .cbor_encode_boolean(has_blob)
                                    .cbor_encode_text_string(blob_path)
                                    .merge(meta.encode())
                                    .merge(bundleEncoder);
                        } else {
                            enc.cbor_start_array(2)
                                    .cbor_start_array(2)
                                    .cbor_encode_boolean(has_blob)
                                    .merge(meta.encode())
                                    .merge(bundleEncoder);
                        }

                        /* bundle and actual serialization */
                        OutputStream out = new BufferedOutputStream(
                                new FileOutputStream(fbundle));
                        AtomicBoolean b = new AtomicBoolean(true);

                        enc.observe().subscribe(
                                buffer -> {
                                    while (buffer.hasRemaining()) {
                                        out.write(buffer.get());
                                    }
                                },
                                e -> b.set(false));
                        out.close();

                        /* if we removed the payload BLOB, we put it back */
                        if(has_blob) {
                            bundle.getPayloadBlock().data = blob;
                        }

                        /* add bundle into index */
                        if (b.get()) {
                            if(has_blob) {
                                getInstance().index.put(bundle.bid,
                                        new IndexEntry(
                                                meta,
                                                fbundle.getAbsolutePath(),
                                                blob_path));
                            } else {
                                getInstance().index.put(bundle.bid,
                                        new IndexEntry(
                                                meta,
                                                fbundle.getAbsolutePath()));
                            }
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

            /* extracting meta */
            IndexEntry meta = getInstance().index.get(id);
            File fbundle = new File(meta.path);
            if (!fbundle.exists() || !fbundle.canRead()) {
                s.onError(new Throwable("can't read bundle in storage: " + meta.path));
                return;
            }

            /* preparing file and parser */
            AtomicReference<Bundle> ret = new AtomicReference<>();
            AtomicReference<String> blob_path = new AtomicReference<>(null);
            CborParser p = CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (__, ___, item) -> {
                                if(item.has_blob) {
                                    blob_path.set(item.blob_path);
                                }
                            })
                    .cbor_parse_custom_item(
                            BundleV7Parser.BundleItem::new,
                            (__, ___, item) -> {
                                ret.set(item.bundle);
                            });

            ByteBuffer buffer = ByteBuffer.allocate(2048);
            FileChannel in = new FileInputStream(meta.path).getChannel();

            /* extracting bundle */
            try {
                boolean done = false;
                while ((in.read(buffer) > 0) && !done) {
                    buffer.flip();
                    done = p.read(buffer);
                    buffer.clear();
                }
                in.close();
            } catch(RxParserException | IOException rpe) {
                /* should not happen */
            }

            /* return */
            if (ret.get() != null) {
                if(blob_path.get() != null) { /* payload block is a FileBLOB */
                    try {
                        ret.get().getPayloadBlock().data = new FileBLOB(blob_path.get());
                        s.onSuccess(ret.get());
                    } catch(IOException io) {
                        s.onError(new Throwable("can't retrieve payload blob"));
                    }
                } else {
                    s.onSuccess(ret.get());
                }
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

            String error = "";
            IndexEntry meta = getInstance().index.get(id);
            File fbundle = new File(meta.path);
            if (fbundle.exists() && !fbundle.canWrite()) {
                error += "can't access bundle file for deletion";
            } else {
                fbundle.delete();
            }

            if(meta.has_blob) {
                File fblob = new File(meta.blob_path);
                if (fblob.exists() && !fbundle.canWrite()) {
                    error += "can't access payload blob file for deletion";
                } else {
                    fblob.delete();
                }
            }

            getInstance().index.remove(id);
            if(error.length() > 0) {
                s.onError(new Throwable(error));
            } else {
                s.onComplete();
            }
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

    private static class FileItem implements CborParser.ParseableItem {
        FileHeaderItem meta;
        Bundle bundle;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array(2)
                    .cbor_parse_custom_item(
                            FileHeaderItem::new,
                            (__, ___, item) -> meta = item)
                    .cbor_parse_custom_item(
                            BundleV7Parser.BundleItem::new,
                            (__, ___, item) -> bundle = item.bundle);
        }
    }

    private static class FileHeaderItem implements CborParser.ParseableItem {

        MetaBundle meta;
        boolean has_blob;
        String  blob_path;

        @Override
        public CborParser getItemParser() {
            return CBOR.parser()
                    .cbor_open_array((__, ___, size) -> {
                        if(size != 2 && size != 3) {
                            throw new RxParserException("array size is not correct");
                        }})
                    .cbor_parse_boolean((p,b) -> {
                        has_blob = b;
                    })
                    .do_insert_if(
                            (__) -> has_blob,
                            CBOR.parser().cbor_parse_text_string_full((__, str) -> blob_path = str)
                    )
                    .cbor_parse_custom_item(
                            MetaBundle.MetaBundleItem::new,
                            (__, ___, item) -> meta = item.meta);
        }
    }
}
