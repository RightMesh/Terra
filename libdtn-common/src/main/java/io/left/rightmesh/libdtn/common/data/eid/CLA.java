package io.left.rightmesh.libdtn.common.data.eid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.left.rightmesh.libdtn.common.data.eid.EID.EIDScheme.CLAUNK;

/**
 * @author Lucien Loiseau on 17/10/18.
 */
public abstract class CLA extends BaseEID {

    public String cl_name;
    public String cl_specific;
    public String cl_sink;

    // should only be called by safe constructor, no validity check
    protected CLA(String cl_name, String cl_specific) {
        this.cl_name = cl_name;
        this.cl_specific = cl_specific;
        this.cl_sink = "";
    }

    CLA(String cl_name, String cl_specific, String sink) throws EIDFormatException {
        this.cl_name = cl_name;
        this.cl_specific = cl_specific;
        this.cl_sink = sink;
        checkValidity();
    }

    public static CLA create(String ssp) throws EIDFormatException {
        final String regex = "^([^:/?#]+):([^/?#]+)(/.*)?";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(ssp);
        if (!m.find()) {
            throw new EIDFormatException("not a CLA");
        }
        String cl_name = m.group(1);
        String cl_specific = m.group(2);
        String cl_sink = m.group(3) == null ? "" : m.group(3);

        if (cl_name.equals("stcp")) {
            return CLASTCP.create(cl_specific, cl_sink);
        } else {
            return new Unknown(cl_name, cl_specific, cl_sink);
        }
    }

    @Override
    public String getScheme() {
        return "cla";
    }

    @Override
    public String getSsp() {
        return cl_name + ":" + cl_specific + cl_sink;
    }


    public String getCLAName() {
        return cl_name;
    }

    public String getCLASpecificPart() {
        return cl_specific;
    }

    public String getPath() {
        return cl_sink;
    }

    @Override
    public boolean matches(EID other) {
        if (other == null) {
            return false;
        }
        if(other instanceof CLA) {
            return cl_specific.equals(((CLA)other).cl_specific)
                    && cl_name.equals(((CLA)other).cl_name);
        }
        return false;
    }

    static class Unknown extends CLA {

        public Unknown(String cl_name, String cl_specific, String sink) throws EIDFormatException {
            super(cl_name, cl_specific, sink);
        }


        @Override
        public int IANA() {
            return EID_CLA_UNK_IANA_VALUE;
        }

        @Override
        public EIDScheme getSchemeCode() {
            return CLAUNK;
        }
    }
}
