package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.events.ChannelOpened;
import io.left.rightmesh.libdtn.network.cla.CLAChannel;
import io.left.rightmesh.libdtn.storage.Storage;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;


/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class RoutingEngine {

    // ---- SINGLETON ----
    private static RoutingEngine instance = new RoutingEngine();
    public static RoutingEngine getInstance() {
        return instance;
    }
    public static void init() {}

    private RoutingEngine() {
        RxBus.register(this);
    }

    public static CLAChannel findCLA(Bundle bundle) {
        CLAChannel ret;
        if((ret = LinkLocalRouting.findCLA(bundle)) != null) {
            return ret;
        }
        if((ret = StaticRouting.findCLA(bundle)) != null) {
            return ret;
        }
        return null;
    }

    public static void forwardLater(final Bundle bundle) {
        bundle.tag("forwardLater", new Object() {
            Bundle b;
            {{
                this.b = bundle;
                RxBus.register(this);
            }}

            @Subscribe
            public void onEvent(ChannelOpened event) {
                if(findCLA(bundle) != null) {
                    RxBus.unregister(this);
                    b.removeTag("eventRegistration");
                    Storage.get(bundle.bid).subscribe(BundleProcessor::bundleForwarding);
                }
            }
        });

        // step-1: read the bundle and extract the CBOR object (a map) for smart routing
        // step-2: for all key, registers the bundle to event ID
        // step-3: for each event the bundle is registered to, pull the bundle from storage,
        //         execute the corresponding bytecode routine.
    }
}
