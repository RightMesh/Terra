package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.data.Bundle;

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
}
