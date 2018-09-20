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
public class EID {

    public static final String RFC3986URIRegExp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    public static class EIDFormatException extends Exception {
        EIDFormatException() {
        }
    }

    public enum EIDScheme {
        DTN,
        IPN,
        UNKNOWN
    }
    EIDScheme scheme_code;

    public int iana_value;
    public String eid;
    public String scheme;
    public String ssp;
    public String authority;
    public String path;
    public String query;
    public String fragment;


    private EID(int iana_value, String str) {
        this.iana_value = iana_value;
        this.eid = str;
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(eid);
        if (m.find()) {
            scheme = m.group(2);
            authority = m.group(4) == null ? "" : m.group(4);
            path      = m.group(5) == null ? "" : m.group(5);
            query     = m.group(7) == null ? "" : m.group(7);
            fragment  = m.group(9) == null ? "" : m.group(9);
            ssp       = authority+path+query+fragment;
        } else {
            scheme = ""; // should never happen because we checked validity beforehand
        }
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
        if(!isValidEID(str)) {
            throw new EIDFormatException();
        }
        EID eid = new EID(127, str);
        if(eid.scheme.equals("dtn")) {
            return new DTN(str);
        }
        if(eid.scheme.equals("ipn")) {
            return new IPN(str);
        } else {
            eid.scheme_code = EIDScheme.UNKNOWN;
            return eid;
        }
    }

    public static EID create(String scheme, String ssp) throws EIDFormatException {
        if(!isValidEID(scheme+":"+ssp)) {
            throw new EIDFormatException();
        }
        if(scheme.equals("dtn")) {
            return new DTN(ssp);
        }
        if(scheme.equals("ipn")) {
            return new IPN(ssp);
        } else {
            EID eid = new EID(127, scheme+":"+ssp);
            eid.scheme_code = EIDScheme.UNKNOWN;
            return eid;
        }
    }

    public static EID.IPN createIPN(int node, int service) {
        return new IPN(node, service);
    }

    public static EID.DTN createDTN(String ssp) {
        return new DTN(ssp);
    }

    public static EID.DTN generate() {
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        return new DTN(uuid);
    }

    public static class DTN extends EID {

        public static int EID_DTN_IANA_VALUE = 1;

        protected DTN(String str) {
            super(EID_DTN_IANA_VALUE, "dtn:"+str);
            this.ssp = str;
        }

        DTN(DTN other) {
            this(other.eid);
            scheme_code = EIDScheme.DTN;
        }
    }

    public int IANA() {
        return iana_value;
    }

    public static class IPN extends EID {

        public static int EID_IPN_IANA_VALUE = 2;

        public int node_number;
        public int service_number;

        protected IPN(String str) throws EIDFormatException {
            super(EID_IPN_IANA_VALUE, str);
            final String regex = "^([0-9]+)\\.([0-9]+)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(ssp);
            if (m.find()) {
                String node = m.group(1);
                String service = m.group(2);
                this.node_number = Integer.valueOf(node);
                this.service_number = Integer.valueOf(service);
            } else {
                throw new EIDFormatException();
            }
        }

        IPN(int node, int service) {
            super(2, "ipn:"+node+"."+service);
            this.node_number = node;
            this.service_number = service;
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
            String authority = m.group(4);
            return (scheme != null) && (!scheme.equals("")) && (authority != null)
                    && (!authority.equals(""));
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
