package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration {

    enum HTTPEntry implements ConfigurationAPI.ModuleEntry {
        COMPONENT_ENABLE_DAEMON_HTTP_API("module_enable_http_daemon"),
        API_DAEMON_HTTP_API_PORT("module_http_port");

        private final String key;

        HTTPEntry(final String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }


    boolean COMPONENT_ENABLE_DAEMON_HTTP_API_DEFAUT = true;
    int API_DAEMON_HTTP_API_PORT_DEFAULT = 8080;
}