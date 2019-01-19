package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * A core component is a component in the library that can be turned on or off. It works by
 * watching a specific entry in the configuration.
 *
 * @author Lucien Loiseau on 29/11/18.
 */
public interface CoreComponentApi {

    class ComponentIsDownException extends Exception {
        public ComponentIsDownException(String componentName) {
            super("component " + componentName + " is down");
        }
    }

    /**
     * get component name.
     *
     * @return name of the component
     */
    String getComponentName();

    /**
     * init a core component. It can be dynamically started or stopped by enabling or disabling
     * the entry in the DTNconfiguration that is passed to this method.
     *
     * @param conf   instance of the configuration
     * @param entry  specific entry to watch for
     * @param logger instance of logger
     */
    void initComponent(ConfigurationApi conf, ConfigurationApi.CoreEntry entry, Log logger);

}
