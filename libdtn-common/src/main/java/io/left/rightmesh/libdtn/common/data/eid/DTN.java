package io.left.rightmesh.libdtn.common.data.eid;

import java.util.UUID;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class DTN extends BaseEID {

    String ssp;

    private DTN() {
    }

    public static DTN generate() {
        DTN ret = new DTN();
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        ret.ssp = uuid;
        return ret;
    }

    /**
     * returns a NULL Endpoint ID.
     *
     * @return EID
     */
    public static DTN NullEID() {
        DTN ret = new DTN();
        ret.ssp = "none";
        return ret;
    }

    public static DTN unsafe(String ssp) {
        DTN ret = new DTN();
        ret.ssp = ssp;
        return ret;
    }

    public static DTN create(String ssp) throws EIDFormatException {
        return new DTN(ssp);
    }

    public DTN(String ssp) throws EIDFormatException  {
        this.ssp = ssp;
        checkValidity();
    }

    @Override
    public EID copy() {
        return unsafe(this.ssp);
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