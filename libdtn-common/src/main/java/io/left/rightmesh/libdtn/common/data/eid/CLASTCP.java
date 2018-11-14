package io.left.rightmesh.libdtn.common.data.eid;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class CLASTCP extends CLA {

    public String host;
    public int port;

    // unsafe constructor
    private CLASTCP(String host, int port) {
        super("stcp", host+":"+port);
        this.host = host;
        this.port = port;
    }

    public static CLASTCP create(String ssp) throws EID.EIDFormatException {
        final String regex = "^([^:/?#]+):([0-9]+)(/.*)?";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(ssp);
        if (!m.find()) {
            throw new EID.EIDFormatException("not an CLASTCP CLA specific host: " + ssp);
        }
        String host = m.group(1);
        int port = Integer.valueOf(m.group(2));
        String cl_sink = m.group(3) == null ? "" : m.group(3);
        return new CLASTCP(host, port, cl_sink);
    }

    public static CLASTCP create(String cl_specific, String cl_sink) throws EID.EIDFormatException {
        final String regex = "^([^:/?#]+):([0-9]+)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(cl_specific);
        if (!m.find()) {
            throw new EID.EIDFormatException("not an CLASTCP CLA specific host: " + cl_specific);
        }
        String host = m.group(1);
        int port = Integer.valueOf(m.group(2));
        return new CLASTCP(host, port, cl_sink);
    }

    public static CLASTCP unsafe(String host, int port) {
        return new CLASTCP(host, port);
    }

    public CLASTCP(String host, int port, String sink) throws EIDFormatException {
        super("stcp", host+":"+port, sink);
        this.host = host;
        this.port = port;
    }

    @Override
    public int IANA() {
        return EID_CLA_IANA_VALUE;
    }

    @Override
    public EIDScheme getSchemeCode() {
        return EIDScheme.CLASTCP;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public EID copy() {
        CLASTCP ret = new CLASTCP(host, port);
        ret.cl_sink = this.cl_sink;
        return ret;
    }

    @Override
    public boolean matches(EID other) {
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
