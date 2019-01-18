package io.left.rightmesh.libdtn.core.spi.aa;

import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.RegistrarAPI;
import io.left.rightmesh.libdtn.core.spi.ModuleSPI;

/**
 * Contract to be fulfilled by an Application Agent Adapter module.
 *
 * @author Lucien Loiseau on 23/10/18.
 */
public interface ApplicationAgentAdapterSPI extends ModuleSPI {

    /**
     * Initialize this module.
     *
     * @param api registrar api
     * @param conf configuration
     * @param logger logger instance
     * @param factory to create new Blob
     */
    void init(RegistrarAPI api,
              ConfigurationAPI conf,
              Log logger,
              ExtensionToolbox toolbox,
              BlobFactory factory);

}
