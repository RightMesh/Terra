package io.left.rightmesh.libdtn.bundleV6;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manipulating RFC5050 Endpoint ID. EIDs are made of two parts:
 * a scheme and a scheme specific part (ssp).
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public class EID implements Comparable<EID> {

    /**
     * returns a NULL Endpoint ID.
     *
     * @return EID
     */
    public static EID NullEID() {
        EID ret = new EID();
        ret.eid = "dtn://null";
        return ret;
    }

    private String eid = null;
    private String scheme = null;
    private String ssp = null;
    private String authority = null;
    private String path = null;

    public class EIDFormatException extends Exception {
        EIDFormatException() {
        }
    }

    private EID() {
    }

    /**
     * generate a random valid EID of the form:
     *
     * <pre>
     *      dtn://RANDOMSSP
     * </pre>
     *
     * <p>with RANDOMSSP being a generated UUID alpha numeric string [A-Za-z0-9]+.
     *
     * @return EID generated
     */
    public static EID generate() {
        return generate(null);
    }

    /**
     * generate a random valid EID with the prefix used a scheme. If the scheme is null or invalid
     * (contains some non alpha character), the scheme "rnd" will be used.
     *
     * @param scheme to be used
     * @return valid EID
     */
    public static EID generate(String scheme) {
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        EID ret = new EID();

        if (scheme == null || !scheme.matches("[A-Za-z]+")) {
            ret.scheme = "dtn";
        } else {
            ret.scheme = scheme;
        }
        ret.ssp = uuid;
        ret.authority = uuid;
        ret.path = "";
        StringBuilder sb = new StringBuilder();
        ret.eid = sb.append(ret.scheme).append("://").append(ret.ssp).toString();
        return ret;
    }

    /**
     * Constructor: creates an EID from a String.
     *
     * @param eid in a String form (scheme://ssp)
     * @throws EIDFormatException if the String does not follow the required format
     */
    public EID(String eid) throws EIDFormatException {
        this.eid = eid;
        if (!isValidEID(this.eid)) {
            throw new EIDFormatException();
        }
    }

    /**
     * Constructor: creates an EID from a scheme and an ssp.
     *
     * @param scheme the scheme part of the EID
     * @param ssp    the scheme specific part of the EID
     * @throws EIDFormatException if the scheme or ssp does not follow the required format
     */
    public EID(String scheme, String ssp) throws EIDFormatException {
        //TODO should adds some format check
        StringBuilder ret = new StringBuilder();
        this.eid = ret.append(scheme).append("://").append(ssp).toString();
        if (!isValidEID(eid)) {
            throw new EIDFormatException();
        }
    }

    /**
     * Constructor: copy an EID from another.
     *
     * @param eid to be copied
     */
    public EID(EID eid) {
        this.eid = eid.eid;
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

    @Override
    public String toString() {
        return this.eid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof EID) {
            EID contact = (EID) o;
            return this.eid.equals(contact.eid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.eid.hashCode();
    }

    @Override
    public int compareTo(EID o) {
        return this.eid.compareTo(o.eid);
    }
}
