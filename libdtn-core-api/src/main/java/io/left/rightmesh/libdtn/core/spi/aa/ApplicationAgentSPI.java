package io.left.rightmesh.libdtn.core.spi.aa;

import io.left.rightmesh.libdtn.core.api.RegistrarAPI;

/**
 * @author Lucien Loiseau on 11/11/18.
 */
public interface ApplicationAgentSPI {

    void init(RegistrarAPI registrar);

}
