package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration extends ConfigurationAPI.ModuleEntry {

    enum LDCPEntry implements ConfigurationAPI.ModuleEntry {

        LDCP_TCP_PORT("ldcp_tcp_port");

        private final String key;

        LDCPEntry(final String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    int LDCP_TCP_PORT_DEFAULT = 4557;

}
