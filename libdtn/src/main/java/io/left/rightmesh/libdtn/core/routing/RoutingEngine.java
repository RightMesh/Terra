package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.data.BundleID;
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

    public static void init() {
    }

    private RoutingEngine() {
        RxBus.register(this);
    }

    public static CLAChannel findCLA(Bundle bundle) {
        CLAChannel ret;
        if ((ret = LinkLocalRouting.findCLA(bundle)) != null) {
            return ret;
        }
        if ((ret = StaticRouting.findCLA(bundle)) != null) {
            return ret;
        }
        return null;
    }

    public static void forwardLater(final Bundle bundle) {
        /* register a listener that will listen for ChannelOpened event
         * and pull the bundle from storage if there is a match */
        final BundleID bid = bundle.bid;
        RxBus.register(new Object() {
            @Subscribe
            public void onEvent(ChannelOpened event) {
                Storage.getMeta(bid).subscribe(
                        meta -> {
                            if (findCLA(bundle) != null) {
                                RxBus.unregister(this);
                                BundleProcessor.bundleActualForward(meta, event.channel);
                            }
                        },
                        e -> RxBus.unregister(this));
            }
        });
    }
}
