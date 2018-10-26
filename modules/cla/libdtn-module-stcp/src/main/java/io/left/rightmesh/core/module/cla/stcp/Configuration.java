package io.left.rightmesh.core.module.cla.stcp;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface Configuration {

    enum STCPEntry {
        COMPONENT_ENABLE_CLA_STCP("component_enable_cla_stcp"),
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

    boolean COMPONENT_ENABLE_CLA_STCP_DEFAULT = true;
    int CLA_STCP_LISTENING_PORT_DEFAULT =  4556;

}
