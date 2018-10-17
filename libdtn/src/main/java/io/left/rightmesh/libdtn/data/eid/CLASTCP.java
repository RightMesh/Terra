package io.left.rightmesh.libdtn.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class CLASTCP extends CLA {

    public String host;
    public int port;

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

    public CLASTCP(String host, int port, String sink) {
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
