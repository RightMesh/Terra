package io.left.rightmesh.module.core.http;

import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration {

    enum HTTPEntry implements ConfigurationAPI.ModuleEntry {
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

    int API_DAEMON_HTTP_API_PORT_DEFAULT = 8080;
}