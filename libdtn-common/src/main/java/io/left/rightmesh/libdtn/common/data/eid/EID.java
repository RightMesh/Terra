package io.left.rightmesh.libdtn.common.data.eid;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manipulating RFC5050 Endpoint ID. EIDs are made of two parts:
 * a scheme and a scheme specific part (ssp).
 *
 * @author Lucien Loiseau on 20/07/18.
 */
public interface EID {

    class EIDFormatException extends Exception {
        EIDFormatException(String msg) {
            super(msg);
        }
    }

    String RFC3986URIRegExp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

    int EID_DTN_IANA_VALUE = 1;
    int EID_IPN_IANA_VALUE = 2;
    int EID_CLA_IANA_VALUE = 3;        // not actually an IANA value
    int EID_CLA_STCP_IANA_VALUE = 50;  // not actually an IANA value
    int EID_API_ME = 251;  // not actually an IANA value
    int EID_CLA_UNK_IANA_VALUE = 252;  // not actually an IANA value
    int EID_UNK_IANA_VALUE = 253;      // not actually an IANA value

    enum EIDScheme {
        DTN(EID_DTN_IANA_VALUE),
        IPN(EID_IPN_IANA_VALUE),
        CLA(EID_CLA_IANA_VALUE),
        CLASTCP(EID_CLA_STCP_IANA_VALUE),
        APIME(EID_API_ME),
        CLAUNK(EID_CLA_IANA_VALUE),
        UNKNOWN(EID_UNK_IANA_VALUE);

        int iana_value;

        EIDScheme(int value) {
            this.iana_value = value;
        }
    }

    static EID create(String str) throws EIDFormatException {
        String scheme;
        String ssp;
        Pattern r = Pattern.compile(RFC3986URIRegExp);
        Matcher m = r.matcher(str);
        if (m.find()) {
            scheme = m.group(2);
            String slashedAuthority = m.group(3) == null ? "" : m.group(3);
            String authority = m.group(4) == null ? "" : m.group(4);
            String path = m.group(5) == null ? "" : m.group(5);
            String undef = m.group(6) == null ? "" : m.group(6);
            String query = m.group(7) == null ? "" : m.group(7);
            String related = m.group(8) == null ? "" : m.group(8);
            String fragment = m.group(9) == null ? "" : m.group(9);
            ssp = slashedAuthority + path + undef + query + related;
        } else {
            throw new EIDFormatException("not a URI");
        }
        if (scheme.equals("api")) {
            return new API(ssp);
        }
        if (scheme.equals("dtn")) {
            return DTN.create(ssp);
        }
        if (scheme.equals("ipn")) {
            return IPN.create(ssp);
        }
        if (scheme.equals("cla")) {
            return CLA.create(ssp);
        }
        return new UnkownEID(127, scheme, ssp);
    }

    int IANA();

    boolean matches(EID other);

    EIDScheme getSchemeCode();

    String getScheme();

    String getSsp();

    String getEIDString();

    /**
     * Check that the EID is a URI as defined in RFC 3986:
     * {@href https://tools.ietf.org/html/rfc3986#appendix-B}
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
