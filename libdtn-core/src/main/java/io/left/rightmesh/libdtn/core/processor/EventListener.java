package io.left.rightmesh.libdtn.core.processor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.common.data.BundleID;
import io.left.rightmesh.libdtn.core.events.BundleDeleted;
import io.left.rightmesh.libdtn.core.utils.Log;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_EVENT_PROCESSING;

/**
 * When an Event is fired, it may trigger some operation to a bundle. For instance, a new
 * ChannelOpenned may represent an opportunity to transmit a bundle, or a RegistrationActive
 * event may trigger multiple bundle to be deliver to the application agent.
 * <p>
 * In order to prevent iterating over the entire bundle index every time there's an event, we allow
 * other component to "register" a bundle to a specific event. The idea is thus that for each
 * event that fires, we can directly access a list of "Bundle of Interest" that will be function
 * of the event parameter. Take for exemple the event RegistrationActive that contains the "sink"
 * value of the registration, an EventListener would thus maintain a list as following:
 *
 * <pre>
 *    Listener for Event RegistrationActive
 *
 *     +-------+-------+-----+-------+
 *     | Sink1 | Sink2 | ... | SinkN |
 *     +-------+-------+-----+-------+
 *         |       |             |
 *         V       V             V
 *     +------+ +------+      +------+
 *     | BID1 | | BID3 |      | BID4 |
 *     +------+ +------+      +------+
 *     | BID2 |               | BID5 |
 *     +------+               +------+
 *                            | BID6 |
 *                            +------+
 * </pre>
 *
 * <p> Similarly for ChannelOpenned Event we would have the following EventListener:
 *
 * <pre>
 *    Listener for Event RegistrationActive
 *
 *     +-------+-------+-----+-------+
 *     | Peer1 | Peer2 | ... | PeerN |
 *     +-------+-------+-----+-------+
 *         |       |             |
 *         V       V             V
 *     +------+ +------+      +------+
 *     | BID1 | | BID1 |      | BID1 |
 *     +------+ +------+      +------+
 *     | BID2 |               | BID5 |
 *     +------+               +------+
 *                            | BID6 |
 *                            +------+
 * </pre>
 *
 * <p>Notice how a Bundle may appear in more than one list ? That is because it may be looking for
 * a direct link-local peer, or any other route, the first match is a win. Such structure provides
 * a short access at the cost of having to hold pointers in memory. the complexity is ~O(NxE) as
 * a bundle is not expected to be register in many different listener nor in many different sublist.
 *
 * <p>now if a bundle is actually transmitted and deleted, we must clear the bundle from all those
 * list hold by the listener.
 *
 * @author Lucien Loiseau on 14/10/18.
 */
public abstract class EventListener<T> {

    private static final String TAG = "EventListener";
    private final Object lock = new Object();
    boolean enabled;
    Map<T, Set<BundleID>> watchList;


    public EventListener() {
        watchList = new ConcurrentHashMap<>();
        DTNConfiguration.<Boolean>get(COMPONENT_ENABLE_EVENT_PROCESSING).observe()
                .subscribe(
                        enabled -> {
                            this.enabled = enabled;
                            if (enabled) {
                                up();
                            } else {
                                down();
                            }
                        },
                        e -> {
                            /* ignore */
                        });
    }

    private boolean up() {
        RxBus.register(this);
        return true;
    }

    private void down() {
        RxBus.unregister(this);
        synchronized (lock) {
            for (T key : watchList.keySet()) {
                Set<BundleID> set = watchList.get(key);
                if (set != null) {
                    set.clear();
                }
            }
            watchList.clear();
        }
    }

    public boolean watch(T key, BundleID bid) {
        if (!enabled) {
            return false;
        }

        synchronized (lock) {
            Log.d(TAG, "add bundle to a watchlist: " + bid.getBIDString() + " key=" + key.toString());
            return watchList.computeIfAbsent(key, k -> new HashSet<>()).add(bid);
        }
    }

    public void unwatch(BundleID bid) {
        if (!enabled) {
            return;
        }

        synchronized (lock) {
            Log.d(TAG, "remove bundle from a watchlist: " + bid.getBIDString());
            for (T key : watchList.keySet()) {
                Set<BundleID> set = watchList.get(key);
                if (set != null) {
                    set.remove(bid);
                }
            }
        }
    }

    public boolean unwatch(T key, BundleID bid) {
        if (!enabled) {
            return false;
        }

        synchronized (lock) {
            Set<BundleID> set = watchList.get(key);
            if (set != null) {
                set.remove(bid);
                return true;
            } else {
                return false;
            }
        }
    }

    public Observable<BundleID> getBundlesOfInterest(T key) {
        if (!enabled) {
            return Observable.empty();
        }

        synchronized (lock) {
            Set<BundleID> set = watchList.get(key);
            if(set == null) {
                return Observable.empty();
            }
            return Observable.fromIterable(new HashSet<>(set));
        }
    }

    @Subscribe
    public void onEvent(BundleDeleted event) {
        unwatch(event.bid);
    }

}