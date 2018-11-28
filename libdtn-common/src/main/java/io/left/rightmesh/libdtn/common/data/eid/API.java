package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class API extends BaseEID {

    public static final int EID_API_IANA_VALUE = 251;  // not actually an IANA value
    public static final String EID_API_SCHEME = "api";  // n

    private String ssp;
    private String path;

    private API() {
        this.ssp = "me";
        this.path = "";
    }

    public static API me() {
        return new API();
    }

    public static class APIParser implements EIDSspParser {
        @Override
        public EID create(String ssp) throws EIDFormatException {
            return new API(ssp);
        }
    }

    public API(String ssp) throws EIDFormatException {
        this();
        final String regex = "me(/.*)?";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(ssp);
        if (m.find()) {
            this.ssp = "me";
            this.path = m.group(1) == null ? "" : m.group(1);
        } else {
            throw new EIDFormatException("not an API:ME");
        }
        checkValidity();
    }

    @Override
    public EID copy() {
        API me = me();
        me.path = path;
        return me;
    }

    @Override
    public int IANA() {
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
    public boolean matches(EID other) {
        if (other == null) {
            return false;
        }
        if(other instanceof API) {
            return true;
        }
        return false;
    }

}
