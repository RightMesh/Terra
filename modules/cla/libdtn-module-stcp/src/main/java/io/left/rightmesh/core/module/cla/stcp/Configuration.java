package io.left.rightmesh.core.module.cla.stcp;

import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration {

    enum STCPEntry implements ConfigurationAPI.ModuleEntry  {
        CLA_STCP_LISTENING_PORT("cla_stcp_port");

        private final String key;

        STCPEntry(final String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    int CLA_STCP_LISTENING_PORT_DEFAULT =  4556;

}
