package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class BundlePulled implements DTNEvent {
    public Bundle bundle;
    public DTNEvent trigger;

    public BundlePulled(Bundle bundle, DTNEvent trigger) {
        this.bundle = bundle;
        this.trigger = trigger;
    }

    @Override
    public String toString() {
        return "Bundle pulled: triger="+trigger.getClass().getName()+" bid="+bundle.bid.getBidString()+" to: "+bundle.getDestination().getEidString();
    }
}
