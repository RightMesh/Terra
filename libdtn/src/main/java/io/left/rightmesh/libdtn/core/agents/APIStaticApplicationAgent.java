package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable.RegistrationCallback;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.storage.blob.BLOB;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIStaticApplicationAgent extends Component {

    public interface StaticAPICallback {
        void recv(BLOB payload);

        void close();
    }

    private static final String TAG = "APIStaticApplicationAgent";

    // ---- SINGLETON ----
    private static APIStaticApplicationAgent instance = new APIStaticApplicationAgent();
    public static APIStaticApplicationAgent getInstance() {  return instance; }
    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_STATIC_API);
    }

    @Override
    protected String getComponentName() {
        return TAG;
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
            public Completable send(Bundle bundle) {
                cb.recv(bundle.getPayloadBlock().data);
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
