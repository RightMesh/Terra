package io.left.rightmesh.core.module.cla.stcp;

import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class CLASTCP extends BaseClaEid {

    String host;
    int port;

    // unsafe constructor
    private CLASTCP(String host, int port) {
        super("stcp", host+":"+port);
        this.host = host;
        this.port = port;
    }

    static CLASTCP unsafe(String host, int port) {
        return new CLASTCP(host, port);
    }

    public CLASTCP(String host, int port, String sink) throws EidFormatException {
        super("stcp", host+":"+port, sink);
        this.host = host;
        this.port = port;
    }

    @Override
    public int ianaNumber() {
        return EID_CLA_IANA_VALUE;
    }

    @Override
    public Eid copy() {
        CLASTCP ret = new CLASTCP(host, port);
        ret.claSink = this.claSink;
        return ret;
    }

    @Override
    public boolean matches(Eid other) {
        if (other == null) {
            return false;
        }
        if (other instanceof CLASTCP) {
            CLASTCP o = (CLASTCP) other;
            return (this.host.equals(o.host) && this.port == o.port);
        }
        return false;
    }
}
