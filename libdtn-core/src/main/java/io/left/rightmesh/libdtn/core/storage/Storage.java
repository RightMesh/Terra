package io.left.rightmesh.libdtn.core.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.blob.NullBLOB;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessorFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.ProcessingException;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.CoreComponent;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.StorageAPI;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY;


/**
 * @author Lucien Loiseau on 29/09/18.
 */
public class Storage extends CoreComponent implements StorageAPI {

    private static final String TAG = "Storage";

    private class CoreBLOBFactory extends BaseBLOBFactory {
        CoreBLOBFactory() {
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
        public BLOB createFileBLOB(int expectedSize) throws BLOBFactoryException {
            try {
                return simpleStorage.createBLOB(expectedSize);
            } catch(StorageAPI.StorageException se) {
                throw new BLOBFactoryException();
            }
        }
    }

    private ConfigurationAPI conf;
    private CoreAPI core;
    private VolatileStorage volatileStorage;
    private SimpleStorage simpleStorage;
    private CoreBLOBFactory blobFactory;
    private BlockProcessorFactory processorFactory;
    private Log logger;

    class IndexEntry {
        Bundle bundle;      /* either a bundle or a metabundle */

        IndexEntry(Bundle bundle) {
            this.bundle = bundle;
        }

        boolean isVolatile = false;
        boolean isPersistent = false;
        String bundle_path; /* path to persistent bundle */
        boolean has_blob;   /* true if payload is also a file */
        String blob_path;   /* path to the payload */
    }
    Map<BundleID, IndexEntry> index = new ConcurrentHashMap<>();

    public Storage(CoreAPI core) {
        this.logger = core.getLogger();
        this.conf = core.getConf();
        this.core = core;
        this.processorFactory = core.getExtensionManager().getBlockProcessorFactory();
        volatileStorage = new VolatileStorage(this, core);
        simpleStorage = new SimpleStorage(this, core);
        blobFactory = new CoreBLOBFactory();
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    public void initComponent(ConfigurationAPI conf, ConfigurationAPI.CoreEntry entry, Log logger) {
        super.initComponent(conf, entry, logger);
        volatileStorage.initComponent(core.getConf(), COMPONENT_ENABLE_VOLATILE_STORAGE, core.getLogger());
        simpleStorage.initComponent(core.getConf(), COMPONENT_ENABLE_SIMPLE_STORAGE, core.getLogger());
    }

    @Override
    protected void componentUp() {
    }

    @Override
    protected void componentDown() {
    }

    @Override
    public BLOBFactory getBlobFactory() {
        if(!isEnabled()) {
            return NullBLOB::new;
        }
        return blobFactory;
    }

    public VolatileStorage getVolatileStorage() {
        return volatileStorage;
    }

    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    IndexEntry addEntry(BundleID bid, Bundle bundle) {
        IndexEntry entry = new IndexEntry(bundle);
        index.put(bid, entry);
        bundle.tag("in_storage");
        return entry;
    }

    IndexEntry getEntryOrCreate(BundleID bid, Bundle bundle) {
        if (contains(bid)) {
            return index.get(bid);
        } else {
            logger.d(TAG, "new entry: " + bid.getBIDString());
            return addEntry(bid, bundle);
        }
    }

    void removeEntry(BundleID bid, IndexEntry entry) {
        if (index.containsKey(bid)) {
            logger.i(TAG, "deleting from storage: " + bid.getBIDString());
            index.remove(bid, entry);
        }
    }

    @Override
    public int count() {
        if(!isEnabled()) {
            return 0;
        }

        return index.size();
    }

    @Override
    public boolean contains(BundleID bid) {
        if(!isEnabled()) {
            return false;
        }

        return index.containsKey(bid);
    }

    /**
     * check if a Bundle is stored in volatile storage
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in volatile storage, false otherwise
     */
    public boolean containsVolatile(BundleID bid) {
        return index.containsKey(bid) && index.get(bid).isVolatile;
    }

    /**
     * check if a Bundle is stored in persistent storage
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in persistent storage, false otherwise
     */
    public boolean containsPersistent(BundleID bid) {
        return index.containsKey(bid) && index.get(bid).isPersistent;
    }

    @Override
    public Single<Bundle> store(Bundle bundle) {
        if(!isEnabled()) {
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
    public Single<Bundle> getMeta(BundleID id) {
        if(!isEnabled()) {
            return Single.error(new ComponentIsDownException(getComponentName()));
        }

        if (!contains(id)) {
            return Single.error(BundleNotFoundException::new);
        } else {
            return Single.just(index.get(id).bundle);
        }
    }

    @Override
    public Single<Bundle> get(BundleID id) {
        if(!isEnabled()) {
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
    public Completable remove(BundleID id) {
        if(!isEnabled()) {
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
            String dest = entry.bundle.getDestination().getEIDString();
            String vol = entry.isVolatile ? "V" : "";
            String per = entry.isPersistent ? "P=" + entry.bundle_path : "";
            sb.append(bid.getBIDString() + "  -  " + dest + "  -  " + vol + " " + per + "\n");
        });
        sb.append("\n");
        return sb.toString();
    }
}
