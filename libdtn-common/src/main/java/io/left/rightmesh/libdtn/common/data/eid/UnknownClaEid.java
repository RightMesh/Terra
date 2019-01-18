package io.left.rightmesh.libdtn.common.data.eid;

/**
 * UnknownClaEid is a helper class to hold a ClaEid whose cla-scheme is unknown and so the
 * ClaEid-scheme-specific part could not be parsed.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public class UnknownClaEid extends BaseClaEid {

    /**
     * create a new UnknownEid by supplying the iana value and the scheme specific part.
     *
     * @param claName     cla scheme
     * @param claSpecific ClaEid scheme-specific part
     * @param sink        of the ClaEid
     * @throws EidFormatException if the scheme-specific part is invalid.
     */
    public UnknownClaEid(String claName, String claSpecific, String sink)
            throws EidFormatException {
        super(claName, claSpecific, sink);
    }

}
