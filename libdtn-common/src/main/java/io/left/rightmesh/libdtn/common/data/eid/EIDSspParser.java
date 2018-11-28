package io.left.rightmesh.libdtn.common.data.eid;

import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public interface EIDSspParser {

    /**
     * parse a scheme specific part and return an EID
     *
     * @param ssp EID-Specific part
     * @return new EID
     * @throws EIDFormatException if scheme is unknown
     */
    EID create(String ssp) throws EIDFormatException;

}
