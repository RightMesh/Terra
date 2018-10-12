package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.events.RegistrationActive;
import io.left.rightmesh.libdtn.storage.blob.BLOB;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_REGISTRATION;


/**
 * Registration Routing keeps track of the registered application agent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class RegistrationTable extends Component {

    private static final String TAG = "RegistrationTable";

    public static class RegistrationIsPassive extends Exception {
    }

    public interface RegistrationCallback {
        boolean isActive();

        Completable send(BLOB payload);

        void close();
    }

    /* passive registration */
    private static RegistrationCallback passiveRegistration = new RegistrationCallback() {
        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public Completable send(BLOB payload) {
            return Completable.error(new RegistrationIsPassive());
        }

        @Override
        public void close() {
        }
    };

    // ---- SINGLETON ----
    private static RegistrationTable instance = new RegistrationTable();
    public static RegistrationTable getInstance() {
        return instance;
    }
    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_REGISTRATION);
    }

    @Override
    protected String getComponentName() {
        return TAG;
    }

    private HashMap<String, RegistrationCallback> registrations;

    @Override
    protected void componentUp() {
        super.componentUp();
        registrations = new HashMap<>();
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        for (String sink : getInstance().registrations.keySet()) {
            getInstance().registrations.get(sink).close();
        }
        getInstance().registrations.clear();
    }

    /**
     * Register an application agent
     *
     * @param sink identifying this AA
     * @param cb   callback to receive payload
     * @return true if the AA was registered, false otherwise
     */
    public static boolean register(String sink, RegistrationCallback cb) {
        if (!getInstance().isEnabled()
                && sink != null
                && cb != null
                && getInstance().registrations.containsKey(sink)) {
            getInstance().registrations.put(sink, cb);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unregister an application agent
     *
     * @param sink identifying the AA to be unregistered
     * @return true if the AA was unregister, false otherwise
     */
    public static boolean unregister(String sink) {
        if (!getInstance().isEnabled() && sink == null
                && getInstance().registrations.containsKey(sink)) {
            getInstance().registrations.get(sink).close();
            getInstance().registrations.remove(sink);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the active registration for a given sink, null otherwise
     * @param sink the registered sink
     * @return RegistrationCallback
     */
    public static RegistrationCallback getRegistration(String sink) {
        if (!getInstance().isEnabled() && sink != null
                && getInstance().registrations.containsKey(sink)) {
            return getInstance().registrations.get(sink);
        } else {
            return passiveRegistration;
        }
    }

    /**
     * Deliver a bundle to the registration
     * @param sink registered
     * @param bundle to deliver
     * @return completes if the bundle was successfully delivered, onError otherwise
     */
    public static Completable deliver(String sink, Bundle bundle) {
        RegistrationCallback cb = getRegistration(sink);
        if (cb.isActive()) {
            return cb.send(bundle.getPayloadBlock().data);
        } else {
            return Completable.error(new RegistrationIsPassive());
        }
    }

    /**
     * Register an EventListener that "wakes up" the bundle if a registration has turned active
     * @param bundle
     */
    public static void deliverLater(final Bundle bundle) {
        final BundleID bid = bundle.bid;
        RxBus.register(new Object() {
            @Subscribe
            public void onEvent(RegistrationActive event) {
                Storage.getMeta(bid).subscribe(
                        BundleProcessor::bundleLocalDelivery,
                        e -> RxBus.unregister(this));
            }
        });
    }
}
