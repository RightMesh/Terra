package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable.RegistrationCallback;
import io.left.rightmesh.libdtn.storage.BLOB;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIStaticAgent extends Component {

    public interface StaticAPICallback {
        void recv(BLOB payload);

        void close();
    }

    // ---- SINGLETON ----
    private static APIStaticAgent instance = new APIStaticAgent();
    public static APIStaticAgent getInstance() {  return instance; }
    public static void init() {}

    APIStaticAgent() {
        super(COMPONENT_ENABLE_STATIC_API);
    }

    public static boolean register(String sink, StaticAPICallback cb) {
        if(!getInstance().isEnabled()) {
            return false;
        }

        return RegistrationTable.register(sink, new RegistrationCallback() {
            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public Completable send(BLOB payload) {
                cb.recv(payload);
                return Completable.complete();
            }

            @Override
            public void close() {
                cb.close();
            }
        });
    }


    public static boolean unregister(String sink, StaticAPICallback cb) {
        if(!getInstance().isEnabled()) {
            return false;
        }

        return RegistrationTable.unregister(sink);
    }

}
