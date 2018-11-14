package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class UnkownEID extends BaseEID {

    private int iana;
    private String scheme;
    private String ssp;

    /* unsafe constructor - no validity check */
    private UnkownEID() {
    }

    public UnkownEID(int iana_value, String scheme, String ssp) throws EIDFormatException {
        this.iana = iana_value;
        this.scheme = scheme;
        this.ssp = ssp;
        if(!EID.isValidEID(this.getEIDString())) {
            throw new EIDFormatException("not an URI");
        }
    }

    @Override
    public EID copy() {
        UnkownEID ret = new UnkownEID();
        ret.iana = iana;
        ret.scheme = scheme;
        ret.ssp = ssp;
        return ret;
    }

    @Override
    public int IANA() {
        return iana;
    }

    @Override
    public EIDScheme getSchemeCode() {
        return EIDScheme.UNKNOWN;
    }

    @Override
    public String getScheme() {
        return scheme;
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
        return scheme.equals(((UnkownEID)other).scheme)
                && ssp.equals(((UnkownEID)other).ssp);
    }
}
