package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class API extends BaseEID {

    String ssp;
    String path;

    private API() {
        this.ssp = "me";
    }

    public static API me() {
        return new API();
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
    public int IANA() {
        return EID_API_ME;
    }

    @Override
    public EIDScheme getSchemeCode() {
        return EIDScheme.APIME;
    }

    @Override
    public String getScheme() {
        return "api";
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
