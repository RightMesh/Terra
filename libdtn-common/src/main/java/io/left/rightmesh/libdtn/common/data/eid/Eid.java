package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manipulating Bundle Protocol Endpoint ID. EIDs are made of two parts:
 * a scheme and a scheme specific part (ssp).
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public interface Eid {

    String RFC3986_URI_REGEXP = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    /**
     * return the ianaNumber number associated with this Eid scheme.
     *
     * @return an int
     */
    int ianaNumber();

    /**
     * returns true if this Eid matches the one given as a parameter.
     * The semantic of "matching" is Eid specific.
     *
     * @param other Eid to match
     * @return true if current Eid matches other Eid, false otherwise
     */
    boolean matches(Eid other);

    /**
     * returns the scheme part of this Eid.
     *
     * @return String
     */
    String getScheme();

    /**
     * returns the scheme-specific part of this Eid.
     *
     * @return String
     */
    String getSsp();

    /**
     * returns the entire Eid string URI.
     *
     * @return String
     */
    String getEidString();

    /**
     * Eid copy.
     *
     * @return a copy of this Eid
     */
    Eid copy();

    /**
     * Check that the Eid is a URI as defined in <a href="https://tools.ietf.org/html/rfc3986#appendix-B">RFC 3986</a>.
     *
     * <p>The Eid is considered valid if there is at least a scheme and a scheme-specific part.
     *
     * @param eid to check validity
     * @return true if valid, false otherwise
     */
    static boolean isValidEid(String eid) {
        Pattern r = Pattern.compile(RFC3986_URI_REGEXP);
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
