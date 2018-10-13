package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.data.Bundle;

/**
 * @author Lucien Loiseau on 13/10/18.
 */
public class BundleAvailable implements DTNEvent {
    public Bundle bundle;
    public String sink;

    BundleAvailable(Bundle b, String sink) {
        this.bundle = b;
        this.sink = sink;
    }
}
