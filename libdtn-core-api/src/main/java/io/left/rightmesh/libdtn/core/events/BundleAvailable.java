package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.common.data.Bundle;

/**
 * BundleAvailable Event. it is thrown whenever a new Bundle is available.
 *
 * @author Lucien Loiseau on 13/10/18.
 */
public class BundleAvailable implements DtnEvent {
    public Bundle bundle;
    public String sink;

    BundleAvailable(Bundle b, String sink) {
        this.bundle = b;
        this.sink = sink;
    }


    @Override
    public String toString() {
        return "New bundle: bid=" + bundle.bid.getBidString()
                + " to: " + bundle.getDestination().getEidString();
    }
}
