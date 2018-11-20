package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;

/**
 * @author Lucien Loiseau on 19/11/18.
 */
public interface ModuleLoaderAPI {

    /**
     * Load an Application Agent Adapter Module
     * @param aa module to load
     */
    void loadAAModule(ApplicationAgentAdapterSPI aa);

    /**
     * Load a Convergence Layer Adapter Module
     * @param cla module to load
     */
    void loadCLAModule(ConvergenceLayerSPI cla);

    /**
     * Load a Core Module
     * @param cm module to load
     */
    void loadCoreModule(CoreModuleSPI cm);

}
