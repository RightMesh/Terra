package io.left.rightmesh.libdtn.core.spi.core;

import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.spi.ModuleSPI;

/**
 * Contract to be fulfilled by a core module. A core module has unrestricted access to all
 * components of the core.
 *
 * @author Lucien Loiseau on 25/10/18.
 */
public interface CoreModuleSPI  extends ModuleSPI {

    /**
     * Initialize this module.
     *
     * @param api core API
     */
    void init(CoreAPI api);

}
