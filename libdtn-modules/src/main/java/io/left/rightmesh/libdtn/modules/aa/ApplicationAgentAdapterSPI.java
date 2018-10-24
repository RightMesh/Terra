package io.left.rightmesh.libdtn.modules.aa;

import io.left.rightmesh.libdtn.modules.RegistrarAPI;

/**
 * Contract to be provided by an application agent adapter.
 *
 * @author Lucien Loiseau on 23/10/18.
 */
public interface ApplicationAgentAdapterSPI {

    void init(RegistrarAPI api);

}
