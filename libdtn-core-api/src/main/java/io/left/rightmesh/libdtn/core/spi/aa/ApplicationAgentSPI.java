package io.left.rightmesh.libdtn.core.spi.aa;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;

/**
 * Contract to be fulfilled by an Application Agent module.
 *
 * @author Lucien Loiseau on 11/11/18.
 */
public interface ApplicationAgentSPI {

    void init(RegistrarAPI registrar, Log logger);

}
