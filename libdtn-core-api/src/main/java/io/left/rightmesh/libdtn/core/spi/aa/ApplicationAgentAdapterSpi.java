package io.left.rightmesh.libdtn.core.spi.aa;

import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.blob.BlobFactory;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.RegistrarApi;
import io.left.rightmesh.libdtn.core.spi.ModuleSpi;

/**
 * Contract to be fulfilled by an Application Agent Adapter module.
 *
 * @author Lucien Loiseau on 23/10/18.
 */
public interface ApplicationAgentAdapterSpi extends ModuleSpi {

    /**
     * Initialize this module.
     *
     * @param api registrar api
     * @param conf configuration
     * @param logger logger instance
     * @param toolbox block and eids factories
     * @param factory to create new Blob
     */
    void init(RegistrarApi api,
              ConfigurationApi conf,
              Log logger,
              ExtensionToolbox toolbox,
              BlobFactory factory);

}
