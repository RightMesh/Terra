package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public class BaseCLAEID extends BaseEID implements CLAEID {

    public String cl_name;
    public String cl_specific;
    public String cl_sink;

    // should only be called by safe constructor, no validity check
    protected BaseCLAEID(String cl_name, String cl_specific) {
        this.cl_name = cl_name;
        this.cl_specific = cl_specific;
        this.cl_sink = "";
    }

    protected BaseCLAEID(String cl_name, String cl_specific, String sink) throws EIDFormatException {
        this.cl_name = cl_name;
        this.cl_specific = cl_specific;
        this.cl_sink = sink;
        checkValidity();
    }

    public BaseCLAEID setPath(String path) throws EIDFormatException {
        if(!path.startsWith("/")) {
            path = "/" + path;
        }
        this.cl_sink = path;
        checkValidity();
        return this;
    }

    @Override
    public int IANA() {
        return CLAEID.EID_CLA_IANA_VALUE;
    }

    @Override
    public String getScheme() {
        return "cla";
    }

    @Override
    public String getSsp() {
        return cl_name + ":" + cl_specific + cl_sink;
    }

    @Override
    public String getCLAName() {
        return cl_name;
    }

    @Override
    public String getCLASpecificPart() {
        return cl_specific;
    }

    @Override
    public String getPath() {
        return cl_sink;
    }

    @Override
    public EID copy() {
        BaseCLAEID copy = new BaseCLAEID(cl_name, cl_specific);
        copy.cl_sink = cl_sink;
        return copy;
    }

    @Override
    public boolean matches(EID other) {
        if (other == null) {
            return false;
        }
        if(other instanceof BaseCLAEID) {
            return cl_specific.equals(((BaseCLAEID)other).cl_specific)
                    && cl_name.equals(((BaseCLAEID)other).cl_name);
        }
        return false;
    }
}
