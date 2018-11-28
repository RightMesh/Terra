package io.left.rightmesh.libdtn.common.data.eid;

import io.left.rightmesh.libdtn.common.data.eid.CLAEID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public interface CLAEIDParser {

    class UnknownCLName extends EIDFormatException {
        public UnknownCLName(String cl_name) {
            super("unknown CL Name: "+cl_name);
        }
    }

    /**
     * BaseCLAEID-specific factory
     *
     * @param cl_name convergence layer name
     * @param cl_specific convergence layer specific host-part
     * @param sink handle may be null
     * @return new BaseCLAEID EID
     * @throws EIDFormatException if Convergence Layer name is unknown
     */
    CLAEID create(String cl_name, String cl_specific, String sink) throws EIDFormatException;
}
