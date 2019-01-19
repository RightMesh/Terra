package io.left.rightmesh.libdtn.core.storage;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleId;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.BaseBlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.Blob;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.data.blob.NullBlob;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.CoreComponent;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.StorageApi;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage class implements StorageAPI and provides both volatile and peristent storage operation
 * based on the node configuration.
 *
 * @author Lucien Loiseau on 29/09/18.
 */
public class Storage extends CoreComponent implements StorageApi {

    private static final String TAG = "Storage";

    private class CoreBlobFactory extends BaseBlobFactory {
        CoreBlobFactory() {
            enableVolatile(conf.<Integer>get(VOLATILE_BLOB_STORAGE_MAX_CAPACITY).value());
            enablePersistent("");
        }

        @Override
        public boolean isVolatileEnabled() {
            return conf.<Boolean>get(COMPONENT_ENABLE_VOLATILE_STORAGE).value();
        }

        @Override
        public boolean isPersistentEnabled() {
            return conf.<Boolean>get(COMPONENT_ENABLE_SIMPLE_STORAGE).value();
        }

        @Override
        public Blob createFileBlob(int expectedSize) throws BlobFactoryException {
            try {
                return simpleStorage.createBlob(expectedSize);
            } catch (StorageApi.StorageException se) {
                throw new BlobFactoryException();
            }
        }
    }

    private ConfigurationApi conf;
    private CoreApi core;
    private VolatileStorage volatileStorage;
    private SimpleStorage simpleStorage;
    private CoreBlobFactory blobFactory;
    private BlockProcessorFactory processorFactory;
    private Log logger;

    class IndexEntry {
        Bundle bundle;      /* either a bundle or a metabundle */

        IndexEntry(Bundle bundle) {
            this.bundle = bundle;
        }

        boolean isVolatile = false;
        boolean isPersistent = false;
        String bundlePath; /* path to persistent bundle */
        boolean hasBlob;   /* true if payload is also a file */
        String blobPath;   /* path to the payload */
    }

    Map<BundleId, IndexEntry> index = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param core reference to the core
     */
    public Storage(CoreApi core) {
        this.logger = core.getLogger();
        this.conf = core.getConf();
        this.core = core;
        this.processorFactory = core.getExtensionManager().getBlockProcessorFactory();
        volatileStorage = new VolatileStorage(this, core);
        simpleStorage = new SimpleStorage(this, core);
        blobFactory = new CoreBlobFactory();
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    public void initComponent(ConfigurationApi conf, ConfigurationApi.CoreEntry entry, Log logger) {
        super.initComponent(conf, entry, logger);
        volatileStorage
                .initComponent(core.getConf(), COMPONENT_ENABLE_VOLATILE_STORAGE, core.getLogger());
        simpleStorage
                .initComponent(core.getConf(), COMPONENT_ENABLE_SIMPLE_STORAGE, core.getLogger());
    }

    @Override
    protected void componentUp() {
    }

    @Override
    protected void componentDown() {
    }

    @Override
    public BlobFactory getBlobFactory() {
        if (!isEnabled()) {
            return NullBlob::new;
        }
        return blobFactory;
    }

    public VolatileStorage getVolatileStorage() {
        return volatileStorage;
    }

    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    IndexEntry addEntry(BundleId bid, Bundle bundle) {
        IndexEntry entry = new IndexEntry(bundle);
        index.put(bid, entry);
        bundle.tag("in_storage");
        return entry;
    }

    IndexEntry getEntryOrCreate(BundleId bid, Bundle bundle) {
        if (contains(bid)) {
            return index.get(bid);
        } else {
            logger.d(TAG, "new entry: " + bid.getBidString());
            return addEntry(bid, bundle);
        }
    }

    void removeEntry(BundleId bid, IndexEntry entry) {
        if (index.containsKey(bid)) {
            logger.i(TAG, "deleting from storage: " + bid.getBidString());
            index.remove(bid, entry);
        }
    }

    @Override
    public int count() {
        if (!isEnabled()) {
            return 0;
        }

        return index.size();
    }

    @Override
    public boolean contains(BundleId bid) {
        if (!isEnabled()) {
            return false;
        }

        return index.containsKey(bid);
    }

    /**
     * check if a Bundle is stored in volatile storage.
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in volatile storage, false otherwise
     */
    boolean containsVolatile(BundleId bid) {
        return index.containsKey(bid) && index.get(bid).isVolatile;
    }

    /**
     * check if a Bundle is stored in persistent storage.
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in persistent storage, false otherwise
     */
    boolean containsPersistent(BundleId bid) {
        return index.containsKey(bid) && index.get(bid).isPersistent;
    }

    @Override
    public Single<Bundle> store(Bundle bundle) {
        if (!isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        if (index.containsKey(bundle.bid)) {
            return Single.error(new BundleAlreadyExistsException());
        }

        /* call block specific routing for storage */
        try {
            for (CanonicalBlock block : bundle.getBlocks()) {
                try {
                    processorFactory.create(block.type).onPutOnStorage(block, bundle, logger);
                } catch (BlockProcessorFactory.ProcessorNotFoundException pe) {
                    /* ignore */
                }
            }
        } catch (ProcessingException e) {
            return Single.error(e);
        }

        return Single.create(s -> volatileStorage.store(bundle).subscribe(
                vb -> simpleStorage.store(vb).onErrorReturnItem(vb)
                        .subscribe(
                                pb -> s.onSuccess(vb),
                                e -> s.onSuccess(vb)),
                e -> simpleStorage.store(bundle)
                        .subscribe(
                                s::onSuccess,
                                s::onError)));
    }

    @Override
    public Single<Bundle> getMeta(BundleId id) {
        if (!isEnabled()) {
            return Single.error(new ComponentIsDownException(getComponentName()));
        }

        if (!contains(id)) {
            return Single.error(BundleNotFoundException::new);
        } else {
            return Single.just(index.get(id).bundle);
        }
    }

    @Override
    public Single<Bundle> get(BundleId id) {
        if (!isEnabled()) {
            return Single.error(new StorageUnavailableException());
        }

        if (containsVolatile(id)) {
            Bundle vb = index.get(id).bundle;

            /* call block specific routine when bundle is pulled from volatile storage */
            try {
                for (CanonicalBlock block : vb.getBlocks()) {
                    try {
                        processorFactory.create(block.type).onPullFromStorage(block, vb, logger);
                    } catch (BlockProcessorFactory.ProcessorNotFoundException pe) {
                        /* ignore */
                    }
                }
            } catch (ProcessingException e) {
                return Single.error(e);
            }

            return Single.just(vb);
        } else {
            return simpleStorage.get(id);
        }
    }

    @Override
    public Completable remove(BundleId id) {
        if (!isEnabled()) {
            return Completable.error(new StorageUnavailableException());
        }

        if (!contains(id)) {
            return Completable.error(BundleNotFoundException::new);
        }

        if (containsPersistent(id)) {
            return simpleStorage.remove(id)
                    .onErrorComplete()
                    .andThen(volatileStorage.remove(id));
        } else {
            return volatileStorage.remove(id);
        }
    }

    @Override
    public Completable clear() {
        return Observable.fromIterable(index.keySet())
                .flatMapCompletable(bid -> remove(bid))
                .onErrorComplete();
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder("current cache:\n");
        sb.append("--------------\n\n");
        index.forEach((bid, entry) -> {
            String dest = entry.bundle.getDestination().getEidString();
            String vol = entry.isVolatile ? "V" : "";
            String per = entry.isPersistent ? "P=" + entry.bundlePath : "";
            sb.append(bid.getBidString() + "  -  " + dest + "  -  " + vol + " " + per + "\n");
        });
        sb.append("\n");
        return sb.toString();
    }
}
