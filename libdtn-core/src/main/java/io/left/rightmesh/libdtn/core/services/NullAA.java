package io.left.rightmesh.libdtn.core.services;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentSPI;
import io.reactivex.Completable;

/**
 * Any Bundle sent to the Null Application Agent will be successfully delivered but deleted
 * immediately.
 *
 * @author Lucien Loiseau on 11/11/18.
 */
public class NullAA implements ApplicationAgentSPI {

    @Override
    public void init(RegistrarAPI registrar, Log logger) {
        try {
            registrar.register("/null/", (bundle) -> {
                bundle.clearBundle();
                return Completable.complete();
            });
        } catch (RegistrarAPI.RegistrarDisabled |
                RegistrarAPI.NullArgument |
                RegistrarAPI.SinkAlreadyRegistered e) {
            /* ignore */
        }
    }

}
