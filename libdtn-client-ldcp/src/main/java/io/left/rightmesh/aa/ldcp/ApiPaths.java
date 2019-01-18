package io.left.rightmesh.aa.ldcp;

/**
 * LDCP Client API Version 1.
 *
 * @author Lucien Loiseau on 01/11/18.
 */
public interface ApiPaths {

    enum ClientToDaemonLdcpPathVersion1 {
        ISREGISTERED("/isregistered/"),
        REGISTER("/register/"),
        UPDATE("/register/update/"),
        UNREGISTER("/unregister/"),
        GETBUNDLE("/get/bundle/"),
        FETCHBUNDLE("/fetch/bundle/"),
        DISPATCH("/dispatch/");

        public String path;

        ClientToDaemonLdcpPathVersion1(String path) {
            this.path = path;
        }
    }

    enum DaemonToClientLdcpPathVersion1 {
        DELIVER("/deliver/");

        public String path;

        DaemonToClientLdcpPathVersion1(String path) {
            this.path = path;
        }
    }

}
