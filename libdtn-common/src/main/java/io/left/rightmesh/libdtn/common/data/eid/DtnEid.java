package io.left.rightmesh.libdtn.common.data.eid;

import java.util.UUID;

/**
 * DtnEid is a class of Eid whose scheme is "dtn".
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public class DtnEid extends BaseEid {

    public static final int EID_DTN_IANA_VALUE = 1;
    public static final String EID_DTN_SCHEME = "dtn";

    private String ssp;

    private DtnEid() {
    }

    /**
     * generate a random and valid DtnEid.
     *
     * @return a new DtnEid.
     */
    public static DtnEid generate() {
        DtnEid ret = new DtnEid();
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        ret.ssp = uuid;
        return ret;
    }

    /**
     * returns a NULL Endpoint ID.
     *
     * @return Eid
     */
    public static DtnEid nullEid() {
        DtnEid ret = new DtnEid();
        ret.ssp = "none";
        return ret;
    }

    /**
     * creates a new DtnEid from a string but do not check for validity.
     *
     * @param ssp scheme specific part of a dtn eid.
     * @return a new DtnEid.
     */
    public static DtnEid unsafe(String ssp) {
        DtnEid ret = new DtnEid();
        ret.ssp = ssp;
        return ret;
    }

    /**
     * Class to create a new DtnEid after parsing the dtn specific part.
     */
    public static class DtnParser implements EidSspParser {
        @Override
        public Eid create(String ssp) throws EidFormatException {
            return new DtnEid(ssp);
        }
    }

    /**
     * safe constructor that creates a DtnEid from a dtn specific part and checks for validity.
     *
     * @param ssp dtn scheme specific part.
     * @throws EidFormatException if the eid is invalid.
     */
    public DtnEid(String ssp) throws EidFormatException {
        this.ssp = ssp;
        checkValidity();
    }

    @Override
    public Eid copy() {
        return unsafe(this.ssp);
    }

    @Override
    public int ianaNumber() {
        return EID_DTN_IANA_VALUE;
    }

    @Override
    public String getScheme() {
        return EID_DTN_SCHEME;
    }

    @Override
    public String getSsp() {
        return ssp;
    }

    @Override
    public boolean matches(Eid other) {
        if (other == null) {
            return false;
        }
        if (other instanceof DtnEid) {
            return ssp.startsWith(((DtnEid) other).ssp);
        }
        return false;
    }

}