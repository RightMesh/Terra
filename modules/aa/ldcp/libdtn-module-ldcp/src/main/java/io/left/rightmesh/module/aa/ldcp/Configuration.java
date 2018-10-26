package io.left.rightmesh.module.aa.ldcp;

import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration extends ConfigurationAPI.ModuleEntry {

    enum LDCPEntry implements ConfigurationAPI.ModuleEntry {

        MODULE_ENABLE_LDCP_DAEMON("module_enable_ldcp_daemon"),
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

    boolean MODULE_ENABLE_LDCP_DAEMON_DEFAULT = false;
    int LDCP_TCP_PORT = 4557;

}
