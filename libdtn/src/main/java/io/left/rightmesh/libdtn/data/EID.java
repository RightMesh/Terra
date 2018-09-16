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

    public static class EIDFormatException extends Exception {
        EIDFormatException() {
        }
    }

    enum EIDScheme {
        DTN,
        IPN,
        UNKNOWN
    }
    EIDScheme scheme_code;

    String eid;
    String scheme;
    String ssp;
    String authority;
    String path;

    private EID() {
    }

    private EID(String str) {
        this.eid = str;
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
        EID eid = new EID(str);
        if(eid.getScheme().equals("dtn")) {
            return new DTN(str);
        }
        if(eid.getScheme().equals("ipn")) {
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
            EID eid = new EID();
            eid.eid = scheme+":"+ssp;
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

        protected DTN(String str) {
            super(str);
        }

        DTN(DTN other) {
            super(other.eid);
            scheme_code = EIDScheme.DTN;
        }
    }

    public static class IPN extends EID {
        int node_number;
        int service_number;

        protected IPN(String str) throws EIDFormatException {
            super(str);
            final String regex = "^([0-9]+)\\.([0-9]+)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(getSsp());
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
            super("ipn:"+node+":"+service);
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
        final String regex = "^(([^:/?#]+):)(//([^/?#]*))([^?#]*)(\\?([^#]*))?(#(.*))?";
        Pattern r = Pattern.compile(regex);
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

    /**
     * get the scheme part of the current EID.
     *
     * @return the scheme part
     */
    public String getScheme() {
        if (scheme == null) {
            final String regex = "^(([^:/?#]+):)(//([^/?#]*))([^?#]*)(\\?([^#]*))?(#(.*))?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(eid);
            if (m.find()) {
                scheme = m.group(2);
            } else {
                scheme = ""; // should never happen because we checked validity beforehand
            }
        }
        return scheme;
    }

    /**
     * get the scheme specific part of the current EID.
     *
     * @return the scheme specific part
     */

    public String getSsp() {
        if (ssp == null) {
            final String regex = "^(([^:/?#]+):)(//(.*))";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(eid);
            if (m.find()) {
                ssp = m.group(4);
            } else {
                ssp = ""; // should never happen because we checked validity beforehand
            }
        }
        return authority;
    }

    /**
     * get the authority part of the current EID.
     *
     * @return the authority part
     */
    public String getAuthority() {
        if (authority == null) {
            final String regex = "^(([^:/?#]+):)(//([^/?#]*))([^?#]*)(\\?([^#]*))?(#(.*))?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(eid);
            if (m.find()) {
                authority = m.group(4);
            } else {
                authority = ""; // should never happen because we checked validity beforehand
            }
        }
        return authority;
    }

    /**
     * get the path part of the current EID.
     *
     * @return the path specific part
     */
    public String getPath() {
        if (path == null) {
            final String regex = "^(([^:/?#]+):)(//([^/?#]*))([^?#]*)(\\?([^#]*))?(#(.*))?";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(eid);
            if (m.find()) {
                path = m.group(5);
            } else {
                path = "/";
            }
        }
        return path;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof DTN) {
            return this.ssp.equals(((DTN) o).ssp);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.ssp.hashCode();
    }
}
