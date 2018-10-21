package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class DTN extends BaseEID {

    String ssp;

    /**
     * returns a NULL Endpoint ID.
     *
     * @return EID
     */
    public static DTN NullEID() {
        DTN ret = new DTN("none");
        return ret;
    }

    public static DTN create(String ssp) {
        return new DTN(ssp);
    }

    public DTN(String ssp) {
        this.ssp = ssp;
    }

    @Override
    public int IANA() {
        return EID_DTN_IANA_VALUE;
    }

    @Override
    public EIDScheme getSchemeCode() {
        return EIDScheme.DTN;
    }

    @Override
    public String getScheme() {
        return "dtn";
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
        if(other instanceof DTN) {
            return ssp.startsWith(((DTN)other).ssp);
        }
        return false;
    }

}