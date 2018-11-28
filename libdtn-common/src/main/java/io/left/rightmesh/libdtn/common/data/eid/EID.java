package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manipulating RFC5050 Endpoint ID. EIDs are made of two parts:
 * a scheme and a scheme specific part (ssp).
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public interface EID {

    String RFC3986URIRegExp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    /**
     * return the IANA number associated with this EID scheme
     *
     * @return an int
     */
    int IANA();

    /**
     * returns true if this EID matches the one given as a parameter.
     * The semantic of "matching" is EID specific.
     *
     * @param other EID to match
     * @return true if current EID matches other EID, false otherwise
     */
    boolean matches(EID other);

    /**
     * returns the scheme part of this EID.
     *
     * @return String
     */
    String getScheme();

    /**
     * returns the scheme-specific part of this EID.
     *
     * @return String
     */
    String getSsp();

    /**
     * returns the entire EID string URI.
     *
     * @return String
     */
    String getEIDString();

    /**
     * EID copy.
     *
     * @return a copy of this EID
     */
    EID copy();

    /**
     * Check that the EID is a URI as defined in <a href="https://tools.ietf.org/html/rfc3986#appendix-B">RFC 3986</a>.
     *
     * <p>The EID is considered valid if there is at least a scheme and a scheme-specific part.
     *
     * @param eid to check validity
     * @return true if valid, false otherwise
     */
    static boolean isValidEID(String eid) {
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(eid);
        if (m.find()) {
            String scheme = m.group(2);
            String authority = m.group(4) == null ? "" : m.group(4);
            String path = m.group(5) == null ? "" : m.group(5);
            String query = m.group(7) == null ? "" : m.group(7);
            String fragment = m.group(9) == null ? "" : m.group(9);
            String ssp = authority + path + query + fragment;
            return (scheme != null) && (!scheme.equals("")) && (!ssp.equals(""));
        } else {
            return false;
        }
    }
}
