package io.left.rightmesh.libdtn.core.routing.strategies.direct;

import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.events.LinkLocalEntryUp;
import io.left.rightmesh.libdtn.core.storage.EventListener;
import io.left.rightmesh.librxbus.Subscribe;

/**
 * DirectRoutingListener tracks and groups bundle together and listen for link-up event to
 * forward bundle.
 *
 * @author Lucien Loiseau on 19/01/19.
 */
public class DirectRoutingListener extends EventListener<String> {

    public static final String TAG = "DirectRoutingListener";

    public DirectRoutingListener(CoreApi core) {
        super(core);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    /**
     * Listen for new peer event and forward relevant bundle accordingly.
     *
     * @param event new peer
     */
    // CHECKSTYLE IGNORE LineLength
    @Subscribe
    public void onEvent(LinkLocalEntryUp event) {
        /* deliver every bundle of interest */
        core.getLogger().i(TAG, "step 1: get all bundleOfInterest "
                + event.channel.channelEid().getClaSpecificPart());
        getBundlesOfInterest(event.channel.channelEid().getClaSpecificPart()).subscribe(
                bundleID -> {
                    core.getLogger().v(TAG, "step 1.1: pull from storage "
                            + bundleID.getBidString());
                    core.getStorage().get(bundleID).subscribe(
                            bundle -> {
                                core.getLogger().v(TAG,
                                        "step 1.2-1: forward bundle "
                                        + bundleID.getBidString());
                                event.channel.sendBundle(
                                        bundle,
                                        core.getExtensionManager().getBlockDataSerializerFactory()
                                ).ignoreElements().subscribe(
                                        () -> {
                                            core.getLogger().v(TAG, "step 1.3: forward successful, resume processing " + bundleID.getBidString());
                                            this.unwatch(bundle.bid);
                                            core.getBundleProtocol()
                                                    .bundleForwardingSuccessful(bundle);
                                        },
                                        e -> {
                                            /* do nothing and wait for next opportunity */
                                            core.getLogger().v(TAG, "step 1.3: forward failed, wait next opportunity " + bundleID.getBidString());
                                        });
                            },
                            e -> {
                                core.getLogger().w(TAG,
                                        "step 1.2-2: failed to pull bundle from storage " + bundleID.getBidString() + ": " + e.getLocalizedMessage());
                            });
                });
    }
    // CHECKSTYLE END IGNORE LineLength
}

