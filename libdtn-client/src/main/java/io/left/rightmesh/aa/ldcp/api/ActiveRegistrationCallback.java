package io.left.rightmesh.aa.ldcp.api;

import io.left.rightmesh.libdtn.common.data.Bundle;
import io.reactivex.Completable;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface ActiveRegistrationCallback {

    /**
     * recv is called whenever some data are available to the current active registration.
     * If this method returns true, the bundle holding this data will be considered as
     * delivered and may trigger the sending of status report. If this method returns
     * false, the bundle may stay in cache until it is either pulled manually by the
     * application agent or deleted by the core.
     *
     * @param bundle to deliver
     * @return true if delivery is accepted, false otherwise.
     */
    Completable recv(Bundle bundle);

}
