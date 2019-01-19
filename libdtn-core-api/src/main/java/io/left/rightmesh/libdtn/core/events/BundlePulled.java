package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * BundlePulled event is thrown whener a bundle is pulled from storage.
 *
 * @author Lucien Loiseau on 10/10/18.
 */
public class BundlePulled implements DtnEvent {
    public Bundle bundle;
    public DtnEvent trigger;

    public BundlePulled(Bundle bundle, DtnEvent trigger) {
        this.bundle = bundle;
        this.trigger = trigger;
    }

    @Override
    public String toString() {
        return "Bundle pulled: triger=" + trigger.getClass().getName()
                + " bid=" + bundle.bid.getBidString() + " to: "
                + bundle.getDestination().getEidString();
    }
}
