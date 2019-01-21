package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ApiEid are a class of Eid used for "api" related services. For instance api:me always refer
 * to the local eid configured by the current node administrator.
 *
 * @author Lucien Loiseau on 29/10/18.
 */
public class ApiEid extends BaseEid {

    public static final int EID_API_IANA_VALUE = 251;  // not actually an ianaNumber value
    public static final String EID_API_SCHEME = "api";  // n

    private String ssp;
    private String path;

    private ApiEid() {
        this.ssp = "me";
        this.path = "";
    }

    public static ApiEid me() {
        return new ApiEid();
    }

    /**
     * Scheme-specific parser for the "api" scheme.
     */
    public static class ApiParser implements EidSspParser {
        @Override
        public Eid create(String ssp) throws EidFormatException {
            return new ApiEid(ssp);
        }
    }

    /**
     * Constructor. Creates an ApiEid after parsing the api-specific part.
     *
     * @param ssp api-specific part.
     * @throws EidFormatException if the api-specific part is not recognized.
     */
    public ApiEid(String ssp) throws EidFormatException {
        this();
        final String regex = "me(/.*)?";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(ssp);
        if (m.find()) {
            this.ssp = "me";
            this.path = m.group(1) == null ? "" : m.group(1);
        } else {
            throw new EidFormatException("not an ApiEid");
        }
        checkValidity();
    }

    @Override
    public Eid copy() {
        ApiEid me = me();
        me.path = path;
        return me;
    }

    @Override
    public int ianaNumber() {
        return EID_API_IANA_VALUE;
    }

    @Override
    public String getScheme() {
        return EID_API_SCHEME;
    }

    @Override
    public String getSsp() {
        return ssp + path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean matches(Eid other) {
        if (other == null) {
            return false;
        }
        if (other instanceof ApiEid) {
            return true;
        }
        return false;
    }

}
