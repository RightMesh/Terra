package io.left.rightmesh.libdtn.core.spi.aa;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;

/**
 * Contract to be fulfilled by an Application Agent module.
 *
 * @author Lucien Loiseau on 11/11/18.
 */
public interface ApplicationAgentSpi {

    void init(RegistrarApi registrar, Log logger);

}
