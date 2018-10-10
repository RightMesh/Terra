package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.data.Bundle;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class BundleIndexed implements DTNEvent {

    public static final String id = "ChannelClosed";

    public Bundle bundle;

    public BundleIndexed(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public String getID() {
        return id;
    }
}
