package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.common.data.BundleId;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class BundleDeleted {

    public BundleId bid;

    public BundleDeleted(BundleId bid) {
        this.bid = bid;
    }

    @Override
    public String toString() {
        return "Bundle deleted: bid="+bid.getBidString();
    }
}
