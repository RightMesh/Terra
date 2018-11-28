package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public class UnknownCLAEID extends BaseCLAEID {

    public UnknownCLAEID(String cl_name, String cl_specific, String sink) throws EIDFormatException {
        super(cl_name, cl_specific, sink);
    }

}
