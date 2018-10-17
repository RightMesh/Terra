package io.left.rightmesh.libdtn.storage.bundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.utils.Log;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * @author Lucien Loiseau on 29/09/18.
 */
public class Storage {

    private static final String TAG = "Storage";

    // ---- SINGLETON ----
    private static Storage instance;

    public static Storage getInstance() {
        return instance;
    }

    static {
        instance = new Storage();
        VolatileStorage.getInstance();
        SimpleStorage.getInstance();
    }

    static class IndexEntry {
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

    static Map<BundleID, IndexEntry> index = new ConcurrentHashMap<>();

    static IndexEntry addEntry(BundleID bid, Bundle bundle) {
        IndexEntry entry = new IndexEntry(bundle);
        index.put(bid, entry);
        bundle.tag("in_storage");
        return entry;
    }

    static IndexEntry getEntryOrCreate(BundleID bid, Bundle bundle) {
        if (contains(bid)) {
            return index.get(bid);
        } else {
            Log.i(TAG, "new entry: " + bid.getBIDString());
            return addEntry(bid, bundle);
        }
    }

    static void removeEntry(BundleID bid, IndexEntry entry) {
        if (index.containsKey(bid)) {
            Log.i(TAG, "deleting from storage: " + bid.getBIDString());
            index.remove(bid, entry);
        }
    }

    /**
     * count the total number of bundle indexed, wether in persistant or volatile storage
     */
    public static int count() {
        return index.size();
    }

    /**
     * check if a Bundle is in storage
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in volatile storage, false otherwise
     */
    public static boolean contains(BundleID bid) {
        return index.containsKey(bid);
    }


    /**
     * check if a Bundle is stored in volatile storage
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in volatile storage, false otherwise
     */
    public static boolean containsVolatile(BundleID bid) {
        return index.containsKey(bid) && index.get(bid).isVolatile;
    }

    /**
     * check if a Bundle is stored in persistent storage
     *
     * @param bid of the bundle
     * @return true if the Bundle is stored in persistent storage, false otherwise
     */
    public static boolean containsPersistent(BundleID bid) {
        return index.containsKey(bid) && index.get(bid).isPersistent;
    }

    /**
     * Try to store in volatile storage first and then copy in persistent storage whatever happens
     * If Volatile Storage is enabled, it will return the whole Bundle, otherwise it returns
     * a MetaBundle.
     * <p>
     * Whenever the Single completes, the caller can expect that no further operations is needed
     * in background to store the bundle.
     *
     * @param bundle to store
     * @return Completable that complete whenever the bundle is stored, error otherwise
     */
    public static Single<Bundle> store(Bundle bundle) {
        if (index.containsKey(bundle.bid)) {
            return Single.error(new BundleStorage.BundleAlreadyExistsException());
        }
        return Single.create(s -> VolatileStorage.store(bundle).subscribe(
                vb -> SimpleStorage.store(vb).onErrorReturnItem(vb)
                        .subscribe(
                                pb -> s.onSuccess(vb),
                                e -> s.onSuccess(vb)),
                e -> SimpleStorage.store(bundle)
                        .subscribe(
                                s::onSuccess,
                                s::onError)));
    }

    /**
     * Pull a MetaBundle from Storage. If the bundle is available in VolatileStorage it pulls
     * the real Bundle from the index, otherwise it returns the MetaBundle.
     *
     * @param id of the bundle to pull from storage
     * @return a Bundle, either a real one or a MetaBundle
     */
    public static Single<Bundle> getMeta(BundleID id) {
        if (!contains(id)) {
            return Single.error(BundleStorage.BundleNotFoundException::new);
        } else {
            return Single.just(index.get(id).bundle);
        }
    }

    /**
     * Pull a Bundle from StorageAPI. It will try to pull it from Volatile if it exists, or from
     * SimpleStorage otherwise.
     *
     * @param id of the bundle to pull from storage
     * @return a Single that completes if the Bundle was successfully pulled, onError otherwise
     */
    public static Single<Bundle> get(BundleID id) {
        if (containsVolatile(id)) {
            return Single.just(index.get(id).bundle);
        } else {
            return SimpleStorage.get(id);
        }
    }

    /**
     * Delete a Bundle from all storage and removes all event registration to it.
     *
     * @param id of the bundle to delete
     * @return Completable
     */
    public static Completable remove(BundleID id) {
        if (!contains(id)) {
            return Completable.error(BundleStorage.BundleNotFoundException::new);
        }

        if (containsPersistent(id)) {
            return SimpleStorage.remove(id)
                    .onErrorComplete()
                    .andThen(VolatileStorage.remove(id));
        } else {
            return VolatileStorage.remove(id);
        }
    }

    /**
     * Clear all bundles
     */
    public static Completable clear() {
        return Observable.fromIterable(index.keySet())
                .flatMapCompletable(bid -> remove(bid))
                .onErrorComplete();
    }

    public static String print() {
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
