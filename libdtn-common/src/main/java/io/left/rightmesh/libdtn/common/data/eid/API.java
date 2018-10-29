package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class API extends BaseEID {

    String ssp;

    private API() {
        this.ssp = "me";
    }

    public static API me() {
        return new API();
    }

    public API(String ssp) throws EIDFormatException {
        this.ssp = ssp;
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
        return ssp;
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
