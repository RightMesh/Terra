package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class BundleIndexed implements DTNEvent {
    public Bundle bundle;

    public BundleIndexed(Bundle bundle) {
        this.bundle = bundle;
    }
}
