package io.left.rightmesh.libdtn.common.data.eid;

/**
 * BaseClaEid implements some of the common ClaEid operations.
 *
 * @author Lucien Loiseau on 17/10/18.
 */
public class BaseClaEid extends BaseEid implements ClaEid {

    public String claName;
    public String claSpecific;
    public String claSink;

    // should only be called by safe constructor, no validity check
    protected BaseClaEid(String claName, String claSpecific) {
        this.claName = claName;
        this.claSpecific = claSpecific;
        this.claSink = "";
    }

    protected BaseClaEid(String claName, String claSpecific, String sink)
            throws EidFormatException {
        this.claName = claName;
        this.claSpecific = claSpecific;
        this.claSink = sink;
        checkValidity();
    }

    /**
     * setPath sets the path part of the ClaEid. The path provided must be a URI-compatible path.
     *
     * @param path to set
     * @return current ClaEid.
     * @throws EidFormatException if the path is invalid.
     */
    public BaseClaEid setPath(String path) throws EidFormatException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        this.claSink = path;
        checkValidity();
        return this;
    }

    @Override
    public int ianaNumber() {
        return ClaEid.EID_CLA_IANA_VALUE;
    }

    @Override
    public String getScheme() {
        return "cla";
    }

    @Override
    public String getSsp() {
        return claName + ":" + claSpecific + claSink;
    }

    @Override
    public String getClaName() {
        return claName;
    }

    @Override
    public String getClaSpecificPart() {
        return claSpecific;
    }

    @Override
    public String getPath() {
        return claSink;
    }

    @Override
    public Eid copy() {
        BaseClaEid copy = new BaseClaEid(claName, claSpecific);
        copy.claSink = claSink;
        return copy;
    }

    @Override
    public boolean matches(Eid other) {
        if (other == null) {
            return false;
        }
        if (other instanceof ClaEid) {
            return claSpecific.equals(((BaseClaEid) other).claSpecific)
                    && claName.equals(((BaseClaEid) other).claName);
        }
        return false;
    }
}
