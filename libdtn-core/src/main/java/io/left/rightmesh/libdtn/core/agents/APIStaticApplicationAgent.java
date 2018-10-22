package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.left.rightmesh.libdtn.core.routing.AARegistrar.RegistrationCallback;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.blob.BLOB;
import io.reactivex.Completable;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIStaticApplicationAgent extends BaseComponent {

    public interface StaticAPICallback {
        void recv(BLOB payload);

        void close();
    }

    private static final String TAG = "APIStaticApplicationAgent";
    private DTNCore core;

    public APIStaticApplicationAgent(DTNCore core) {
        this.core = core;
        initComponent(core.getConf(), COMPONENT_ENABLE_STATIC_API);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
    }

    @Override
    protected void componentDown() {
    }

    public boolean register(String sink, StaticAPICallback cb) {
        if(!isEnabled()) {
            return false;
        }

        return core.getRegistrar().register(sink, new RegistrationCallback() {
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

    public boolean unregister(String sink, StaticAPICallback cb) {
        if(!isEnabled()) {
            return false;
        }

        return core.getRegistrar().unregister(sink);
    }

}
