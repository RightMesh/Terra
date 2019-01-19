package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSpi;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSpi;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSpi;

/**
 * API to load modules dynamically into the system.
 *
 * @author Lucien Loiseau on 19/11/18.
 */
public interface ModuleLoaderApi extends CoreComponentApi {

    /**
     * Load an Application Agent Adapter Module.
     * @param aa module to load
     */
    void loadAaModule(ApplicationAgentAdapterSpi aa);

    /**
     * Load a Convergence Layer Adapter Module.
     * @param cla module to load
     */
    void loadClaModule(ConvergenceLayerSpi cla);

    /**
     * Load a Core Module.
     * @param cm module to load
     */
    void loadCoreModule(CoreModuleSpi cm);

}
