package io.left.rightmesh.libdtn.core.processor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.events.BundleDeleted;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_EVENT_PROCESSING;

/**
 * When an Event is fired, it may trigger some operation to a bundle. For instance, a new
 * ChannelOpenned may represent an opportunity to transmit a bundle, or a RegistrationActive
 * event may trigger multiple bundle to be deliver to the application agent.
 *
 * In order to prevent iterating over the entire bundle index every time there's an event, we allow
 * other component to "register" a bundle to a specific event. The idea is thus that for each
 * event that fires, we can directly access a list of "Bundle of Interest" that will be function
 * of the event parameter. Take for exemple the event RegistrationActive that contains the "sink"
 * value of the registration, a Listener would thus maintain a list as following:
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
 * Similarly for ChannelOpenned Event we would have the following Listener:
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
 *
 *
 * @author Lucien Loiseau on 14/10/18.
 */
public class EventProcessor extends Component {

    private static final String TAG = "EventProcessor";

    // ---- SINGLETON ----
    private static EventProcessor instance;

    public static EventProcessor getInstance() {
        return instance;
    }

    static {
        instance = new EventProcessor();
        listeners = new LinkedList<>();
        getInstance().initComponent(COMPONENT_ENABLE_EVENT_PROCESSING);
    }

    private static List<Listener> listeners;

    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        RxBus.unregister(this);
        for(Listener l : listeners) {
            l.down();
        }
    }

    @Subscribe
    public void onEvent(BundleDeleted event) {
        for (Listener l : listeners) {
            l.unwatch(event.bid);
        }
    }

    public abstract static class Listener<T> {
        Map<T, Set<BundleID>> watchList;

        public Listener() {
            watchList = new ConcurrentHashMap<>();
        }

        public boolean up() {
            if(!EventProcessor.instance.isEnabled()) {
                return false;
            }

            if(listeners.contains(this)) {
                return false;
            }

            RxBus.register(this);
            listeners.add(this);
            return true;
        }

        public void down() {
            if(listeners.contains(this)) {
                listeners.remove(this);
                RxBus.unregister(this);
                for(T key : watchList.keySet()) {
                    Set<BundleID> set = watchList.get(key);
                    if(set != null) {
                        set.clear();
                    }
                }
                watchList.clear();
            }
        }

        public boolean watch(T key, BundleID bid) {
            if(!EventProcessor.instance.isEnabled()) {
                return false;
            }

            return watchList.putIfAbsent(key, new HashSet<>())
                    .add(bid);
        }

        void unwatch(BundleID bid) {
            if(!EventProcessor.instance.isEnabled()) {
                return;
            }

            for(T key : watchList.keySet()) {
                Set<BundleID> set = watchList.get(key);
                if(set != null) {
                    set.remove(bid);
                }
            }
        }

        public boolean unwatch(T key, BundleID bid) {
            if(!EventProcessor.instance.isEnabled()) {
                return false;
            }
            Set<BundleID> set = watchList.get(key);
            if (set != null) {
                set.remove(bid);
                return true;
            } else {
                return false;
            }
        }

        public Set<BundleID> getBundlesOfInterest(T key) {
            Set<BundleID> set = watchList.get(key);
            if(set == null) {
                return new HashSet<>();
            } else {
                return set;
            }
        }
    }
}
