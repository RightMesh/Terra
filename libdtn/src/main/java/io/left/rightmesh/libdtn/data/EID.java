package io.left.rightmesh.libdtn.data;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manipulating RFC5050 Endpoint ID. EIDs are made of two parts:
 * a scheme and a scheme specific part (ssp).
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public abstract class EID {

    public static class EIDFormatException extends Exception {
        EIDFormatException() {
        }
        EIDFormatException(String msg) {
            super(msg);
        }
    }

    public static final String RFC3986URIRegExp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    public static int EID_DTN_IANA_VALUE = 1;
    public static int EID_IPN_IANA_VALUE = 2;
    public static int EID_CLA_IANA_VALUE = 3;   // not actually an IANA value
    public static int EID_UNK_IANA_VALUE = 253; // not actually an IANA value

    public enum EIDScheme {
        DTN(EID_DTN_IANA_VALUE),
        IPN(EID_IPN_IANA_VALUE),
        CLA(EID_CLA_IANA_VALUE),
        UNKNOWN(EID_UNK_IANA_VALUE);

        int iana_value;
        EIDScheme(int value) {
            this.iana_value = value;
        }

    }

    EIDScheme scheme_code;
    public String eid;
    public String scheme;
    public String ssp;


    private EID(EIDScheme scheme_code, String scheme, String ssp) {
        this.scheme_code = scheme_code;
        this.scheme = scheme;
        this.ssp = ssp;
        this.eid = scheme+":"+ssp;
    }


    public int IANA() {
        return scheme_code.iana_value;
    }

    /**
     * returns a NULL Endpoint ID.
     *
     * @return EID
     */
    public static EID NullEID() {
        EID ret = new EID.DTN("none");
        return ret;
    }

    public static EID create(String str) throws EIDFormatException {
        String scheme;
        String ssp;
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(str);
        if (m.find()) {
            scheme = m.group(2);
            String slashedAuthority = m.group(3) == null ? "" : m.group(3);
            String authority = m.group(4) == null ? "" : m.group(4);
            String path      = m.group(5) == null ? "" : m.group(5);
            String undef     = m.group(6) == null ? "" : m.group(6);
            String query     = m.group(7) == null ? "" : m.group(7);
            String related   = m.group(8) == null ? "" : m.group(8);
            String fragment  = m.group(9) == null ? "" : m.group(9);
            ssp       = slashedAuthority+path+undef+query+related;
        } else {
            throw new EIDFormatException("not a URI");
        }
        if(scheme.equals("dtn")) {
            return DTN.create(ssp);
        }
        if(scheme.equals("ipn")) {
            return IPN.create(ssp);
        }
        if(scheme.equals("cla")) {
            return CLA.create(ssp);
        }
        return new UNK(127, scheme, ssp);
    }

    public static EID.DTN generate() {
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        return new DTN(uuid);
    }


    public static class UNK extends EID {
        public UNK(int iana_value, String scheme, String ssp) {
            super(EIDScheme.UNKNOWN, scheme, ssp);
        }

        @Override
        public boolean matches(EID other) {
            if(other == null) {
                return false;
            }
            return eid.equals(other.eid);
        }
    }

    public static class DTN extends EID {

        public static DTN create(String ssp) {
            return new DTN(ssp);
        }

        public DTN(String ssp) {
            super(EIDScheme.DTN, "dtn", ssp);
        }

        @Override
        public boolean matches(EID other) {
            if(other == null) {
                return false;
            }
            return eid.startsWith(other.eid);
        }
    }

    public static class IPN extends EID {

        public int node_number;
        public int service_number;

        public static IPN create(String ssp) throws EIDFormatException {
            final String regex = "^([0-9]+)\\.([0-9]+)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (m.find()) {
                String node = m.group(1);
                String service = m.group(2);
                int node_number = Integer.valueOf(node);
                int service_number = Integer.valueOf(service);
                return new IPN(node_number, service_number);
            } else {
                throw new EIDFormatException("not an IPN");
            }
        }

        public IPN(int node, int service) {
            super(EIDScheme.IPN, "ipn", node+"."+service);
            this.node_number = node;
            this.service_number = service;
        }

        @Override
        public boolean matches(EID other) {
            if(other == null) {
                return false;
            }
            if(other instanceof IPN) {
                IPN o = (IPN)other;
                return (node_number == o.node_number && service_number == o.service_number);
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof IPN) {
                return this.node_number == ((IPN) o).node_number
                        && this.service_number == ((IPN) o).service_number;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + node_number;
            hash = hash * 31 + service_number;
            return hash;
        }
    }


    public static abstract class CLA extends EID {

        public String cl_name;
        public String cl_specific;


        public static CLA create(String ssp) throws EIDFormatException {
            final String regex = "^([^:/?#]+):(.*)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (!m.find()) {
                throw new EIDFormatException("not a CLA");
            }
            String cl_name = m.group(1);
            String cl_specific = m.group(2);

            if(cl_name.equals("stcp")) {
                return CLASTCP.create(cl_specific);
            } else {
                return new UNKCLA(cl_name, cl_specific);
            }
        }

        public CLA(String claname, String claSpecificPart) {
            super(EIDScheme.CLA, "cla", claname+":"+claSpecificPart);
            this.cl_name = claname;
            this.cl_specific = claSpecificPart;
        }

        @Override
        public boolean matches(EID other) {
            if(other == null) {
                return false;
            }
            return eid.equals(other.eid);
        }
    }

    public static class CLASTCP extends CLA {
        public String host;
        public int port;
        public String path;

        public static CLA create(String ssp) throws EIDFormatException {
            final String regex = "^([^:/?#]+):([0-9]+)(/.*)?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (!m.find()) {
                throw new EIDFormatException("not an STCP CLA: "+ssp);
            }
            String host = m.group(1);
            int port = Integer.valueOf(m.group(2));
            String path = m.group(3) == null ? "" : m.group(3);
            return new CLASTCP(host, port, path);
        }

        public CLASTCP(String host, int port, String path) {
            super("stcp", host+":"+port+path);
            this.host = host;
            this.port = port;
            this.path = path;
        }

        @Override
        public boolean matches(EID other) {
            if(other == null) {
                return false;
            }
            if(other instanceof CLASTCP) {
                CLASTCP o = (CLASTCP)other;
                return (this.host.equals(o.host) && this.port == o.port);
            }
            return false;
        }
    }

    public static class UNKCLA extends CLA {
        public UNKCLA(String cl_name, String cl_specific) {
            super(cl_name, cl_specific);
        }
    }

    /**
     * Matching between two EIDs is scheme specific
     *
     * @param other
     * @return
     */
    public abstract boolean matches(EID other);


    /**
     * Return EID scheme
     *
     * @return EIDScheme
     */
    public EIDScheme getScheme() {
        return scheme_code;
    }

    /**
     * Check that the EID is a URI as defined in RFC 3986:
     * {@href https://tools.ietf.org/html/rfc3986#appendix-B}
     *
     * <p>The EID is considered valid if there is at least a scheme and a scheme-specific part.
     *
     * @param eid to check validity
     * @return true if valid, false otherwise
     */
    public static boolean isValidEID(String eid) {
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(eid);
        if (m.find()) {
            String scheme = m.group(2);
            String authority = m.group(4) == null ? "" : m.group(4);
            String path      = m.group(5) == null ? "" : m.group(5);
            String query     = m.group(7) == null ? "" : m.group(7);
            String fragment  = m.group(9) == null ? "" : m.group(9);
            String ssp       = authority+path+query+fragment;
            return (scheme != null) && (!scheme.equals("")) && (!ssp.equals(""));
        } else {
            return false;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof DTN) {
            return this.toString().equals(o.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.ssp.hashCode();
    }

    @Override
    public String toString() {
        return eid;
    }
}
