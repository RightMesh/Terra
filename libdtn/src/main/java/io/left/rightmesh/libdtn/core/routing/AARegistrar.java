package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.processor.EventListener;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.events.RegistrationActive;
import io.left.rightmesh.libdtn.storage.bundle.Storage;
import io.left.rightmesh.librxbus.Subscribe;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_AA_REGISTRATION;


/**
 * RegistrationAPI Routing keeps track of the registered application agent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class AARegistrar extends Component {

    private static final String TAG = "AARegistrar";

    // ----- public interface and exception
    public static class RegistrationIsPassive extends Exception {
    }

    public interface RegistrationCallback {
        boolean isActive();

        Completable send(Bundle bundle);

        void close();
    }

    /* passive registration */
    private static RegistrationCallback passiveRegistration = new RegistrationCallback() {
        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public Completable send(Bundle bundle) {
            return Completable.error(new RegistrationIsPassive());
        }

        @Override
        public void close() {
        }
    };

    // ---- SINGLETON ----
    private static AARegistrar instance;
    public static AARegistrar getInstance() { return instance; }

    static {
        instance = new AARegistrar();
        registrations = new HashMap<>();
        listener = new DeliveryListener();
        getInstance().initComponent(COMPONENT_ENABLE_AA_REGISTRATION);
    }

    // ---- Component Specific Override----
    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        for (String sink : registrations.keySet()) {
            registrations.get(sink).close();
        }
        registrations.clear();
    }

    // ----  Business Logic ----
    private static HashMap<String, RegistrationCallback> registrations;
    private static DeliveryListener listener;

    public static class DeliveryListener extends EventListener<String> {
        @Subscribe
        public void onEvent(RegistrationActive active) {
            /* deliver every bundle of interest */
            getBundlesOfInterest(active.sink).forEach(
                    bundleID -> {
                        /* retrieve the bundle - should be constant operation */
                        Storage.getMeta(bundleID).subscribe(
                                /* deliver it */
                                bundle -> active.cb.send(bundle).subscribe(
                                        () -> {
                                            listener.unwatch(active.sink, bundle.bid);
                                            BundleProcessor.bundleLocalDeliverySuccessful(bundle);
                                        },
                                        e -> BundleProcessor.bundleLocalDeliveryFailure(active.sink, bundle)),
                                e -> {});
                    });
        }
    }


    /**
     * Check wether a sink is registered or not
     *
     * @param sink identifying this AA
     * @return true if the AA is registered, false otherwise
     */
    public static boolean isRegistered(String sink) {
        if (getInstance().isEnabled() && sink != null) {
            return registrations.containsKey(sink);
        } else {
            return false;
        }
    }


    /**
     * Register an application agent
     *
     * @param sink identifying this AA
     * @return true if the AA was successfully registered, false otherwise
     */
    public static boolean register(String sink) {
        return register(sink, passiveRegistration);
    }

    /**
     * Register an application agent
     *
     * @param sink identifying this AA
     * @param cb   callback to receive payload
     * @return true if the AA was registered, false otherwise
     */
    public static boolean register(String sink, RegistrationCallback cb) {
        if (getInstance().isEnabled()
                && sink != null
                && cb != null
                && !registrations.containsKey(sink)) {
            registrations.put(sink, cb);
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
        if (getInstance().isEnabled() && sink != null
                && registrations.containsKey(sink)) {
            registrations.get(sink).close();
            registrations.remove(sink);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the active registration for a given sink, null otherwise
     *
     * @param sink the registered sink
     * @return RegistrationCallback
     */
    public static RegistrationCallback getRegistration(String sink) {
        if (getInstance().isEnabled() && sink != null
                && registrations.containsKey(sink)) {
            return registrations.get(sink);
        } else {
            return passiveRegistration;
        }
    }

    /**
     * Deliver a bundle to the registration
     *
     * @param sink   registered
     * @param bundle to deliver
     * @return completes if the bundle was successfully delivered, onError otherwise
     */
    public static Completable deliver(String sink, Bundle bundle) {
        if (!getInstance().isEnabled()) {
            return Completable.error(new Throwable("disabled component"));
        }

        RegistrationCallback cb = getRegistration(sink);
        if (cb.isActive()) {
            return cb.send(bundle);
        } else {
            return Completable.error(new RegistrationIsPassive());
        }
    }


    public static void deliverLater(String sink, final Bundle bundle) {
        listener.watch(sink, bundle.bid);
    }

    /**
     * print the state of the registration table
     *
     * @return String
     */
    public static String printTable() {
        StringBuilder sb = new StringBuilder("\n\ncurrent registration table:\n");
        sb.append("---------------------------\n\n");
        if (getInstance().isEnabled()) {
            for (String sink : registrations.keySet()) {
                sb.append(sink).append(" ");
                if(getRegistration(sink) == passiveRegistration){
                    sb.append("PASSIVE\n");
                } else {
                    sb.append("ACTIVE\n");
                }
            }
        } else {
            sb.append("disabled");
        }
        sb.append("\n");
        return sb.toString();
    }
}
