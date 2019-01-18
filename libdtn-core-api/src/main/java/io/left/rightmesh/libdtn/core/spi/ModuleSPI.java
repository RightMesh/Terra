package io.left.rightmesh.libdtn.core.spi;

/**
 * <p>A module is an extension of the core that can either extend the bundle protocol itself or
 * provide some other services. There are different class of module with respect to what they
 * provide and how much they can access the core components:
 *
 * <ul>
 *     <li>Application Agent Module: this class of module provides a service that sits at the
 *     application layer. They only have access to the Registrar.</li>
 *
 *     <li>Block Module: this class of module provides extension block and has no access to the
 *     core. They only have access to the ExtensionManager to register their new blocks.</li>
 *
 *     <li>Convergence Layer Adapter Module: this class of module provides new convergence layer
 *     adapter. They have no access to the core and only implements the CLA contract. </li>
 *
 *     <li>Core Module: this class can extend the services of the library at a deep level and has
 *     unrestricted access to all the core components.</li>
 * </ul>
 *
 * @author Lucien Loiseau on 26/10/18.
 */
public interface ModuleSPI {

    /**
     * The name for this ModuleSPI.
     *
     * @return a String with the name of this BaseClaEid.
     */
    String getModuleName();

}
