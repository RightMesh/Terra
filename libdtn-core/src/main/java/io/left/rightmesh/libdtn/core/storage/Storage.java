package io.left.rightmesh.libdtn.core.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.left.rightmesh.libdtn.common.data.blob.BLOBFactory;
import io.left.rightmesh.libdtn.common.data.blob.BaseBLOBFactory;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.StorageAPI;
import io.left.rightmesh.libdtn.core.utils.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY;


/**
 * @author Lucien Loiseau on 29/09/18.
 */
public class Storage implements StorageAPI {

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
        public BLOB createFileBLOB() throws BLOBFactoryException {
            try {
                return simpleStorage.createBLOB();
            } catch(StorageAPI.StorageException se) {
                throw new BLOBFactoryException();
            }
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
    private VolatileStorage volatileStorage;
    private SimpleStorage simpleStorage;
    private CoreBLOBFactory blobFactory;
    private Logger logger;

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

    public Storage(ConfigurationAPI conf, Logger logger) {
        this.logger = logger;
        this.conf = conf;
        volatileStorage = new VolatileStorage(this, conf, logger);
        simpleStorage = new SimpleStorage(this, conf, logger);
        blobFactory = new CoreBLOBFactory();
    }

    public BLOBFactory getBlobFactory() {
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

    public int count() {
        return index.size();
    }


    public boolean contains(BundleID bid) {
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

    public Single<Bundle> store(Bundle bundle) {
        if (index.containsKey(bundle.bid)) {
            return Single.error(new BundleAlreadyExistsException());
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

    public Single<Bundle> getMeta(BundleID id) {
        if (!contains(id)) {
            return Single.error(BundleNotFoundException::new);
        } else {
            return Single.just(index.get(id).bundle);
        }
    }

    public Single<Bundle> get(BundleID id) {
        if (containsVolatile(id)) {
            return Single.just(index.get(id).bundle);
        } else {
            return simpleStorage.get(id);
        }
    }

    public Completable remove(BundleID id) {
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

    public Completable clear() {
        return Observable.fromIterable(index.keySet())
                .flatMapCompletable(bid -> remove(bid))
                .onErrorComplete();
    }

    public String print() {
        StringBuilder sb = new StringBuilder("current cache:\n");
        sb.append("--------------\n\n");
        index.forEach((bid, entry) -> {
            String dest = entry.bundle.destination.getEIDString();
            String vol = entry.isVolatile ? "V" : "";
            String per = entry.isPersistent ? "P=" + entry.bundle_path : "";
            sb.append(bid.getBIDString() + "  -  " + dest + "  -  " + vol + " " + per + "\n");
        });
        sb.append("\n");
        return sb.toString();
    }
}
