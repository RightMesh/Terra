package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.common.data.BundleID;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class BundleDeleted {

    public BundleID bid;

    public BundleDeleted(BundleID bid) {
        this.bid = bid;
    }
}
