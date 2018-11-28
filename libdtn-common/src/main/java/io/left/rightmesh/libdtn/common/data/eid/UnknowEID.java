package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class UnknowEID extends BaseEID {

    public static final String EID_UNK_SCHEME = "unk";

    private int iana;
    private String scheme;
    private String ssp;

    /* unsafe constructor - no validity check */
    private UnknowEID() {
    }

    public UnknowEID(int iana_value, String ssp) throws EIDFormatException {
        this.iana = iana_value;
        this.scheme = EID_UNK_SCHEME;
        this.ssp = ssp;
        if(!EID.isValidEID(this.getEIDString())) {
            throw new EIDFormatException("not an URI");
        }
    }

    @Override
    public EID copy() {
        UnknowEID ret = new UnknowEID();
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
        return scheme.equals(((UnknowEID)other).scheme)
                && ssp.equals(((UnknowEID)other).ssp);
    }
}
