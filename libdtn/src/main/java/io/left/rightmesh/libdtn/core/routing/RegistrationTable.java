package io.left.rightmesh.libdtn.core.routing;

import java.util.HashMap;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.storage.BLOB;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_REGISTRATION;


/**
 * Registration Routing keeps track of the registered application agent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class RegistrationTable extends Component {

    public static class RegistrationIsPassive extends Exception {
    }

    public interface RegistrationCallback {
        boolean isActive();

        Completable send(BLOB payload);

        void close();
    }

    public static class PassiveRegistration implements RegistrationCallback {
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
    }

    // ---- SINGLETON ----
    private static RegistrationTable instance = new RegistrationTable();
    public static RegistrationTable getInstance() {
        return instance;
    }
    public static void init() {}

    private RegistrationTable() {
        super(COMPONENT_ENABLE_REGISTRATION);
    }

    private HashMap<String, RegistrationCallback> registrations;

    @Override
    protected void componentUp() {
        registrations = new HashMap<>();
    }

    @Override
    protected void componentDown() {
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
        if (!getInstance().isEnabled()) {
            return false;
        }

        if (sink == null || cb == null) {
            return false;
        }

        if (getInstance().registrations.containsKey(sink)) {
            return false;
        }

        getInstance().registrations.put(sink, cb);
        return true;
    }

    /**
     * Unregister an application agent
     *
     * @param sink identifying the AA to be unregistered
     * @return true if the AA was unregister, false otherwise
     */
    public static boolean unregister(String sink) {
        if (!getInstance().isEnabled()) {
            return false;
        }

        if (sink == null) {
            return false;
        }

        if (getInstance().registrations.containsKey(sink)) {
            getInstance().registrations.get(sink).close();
            getInstance().registrations.remove(sink);
            return true;
        }

        return false;
    }

    public static RegistrationCallback getRegistration(String sink) {
        if (!getInstance().isEnabled()) {
            return new PassiveRegistration();
        }

        if (sink == null) {
            return new PassiveRegistration();
        }

        if (getInstance().registrations.containsKey(sink)) {
            return getInstance().registrations.get(sink);
        }
        return new PassiveRegistration();
    }

    public static Completable deliver(String sink, Bundle bundle) {
        RegistrationCallback cb = getRegistration(sink);
        if (cb.isActive()) {
            return cb.send(bundle.getPayloadBlock().data);
        } else {
            return Completable.error(new RegistrationIsPassive());
        }
    }

    public static void deliverLater(Bundle bundle, String sink) {
        // todo
    }
}
