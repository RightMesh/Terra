package io.left.rightmesh.libdtn.common.data.eid;

/**
 * EidSspParser is an interface to create an Eid after parsing the scheme-specific part.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public interface EidSspParser {

    /**
     * parse a scheme-specific part and return a new Eid if valid.
     *
     * @param ssp Eid-Specific part
     * @return new Eid
     * @throws EidFormatException if scheme is unknown or if scheme-specific part is invalid.
     */
    Eid create(String ssp) throws EidFormatException;

}
