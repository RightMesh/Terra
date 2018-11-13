package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class BundleIndexed implements DTNEvent {
    public Bundle bundle;

    public BundleIndexed(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public String toString() {
        return "Bundle indexed: bid="+bundle.bid.getBIDString()+" to: "+bundle.destination.getEIDString();
    }
}
