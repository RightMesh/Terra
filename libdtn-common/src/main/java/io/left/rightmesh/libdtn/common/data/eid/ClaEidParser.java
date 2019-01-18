package io.left.rightmesh.libdtn.common.data.eid;

/**
 * Interface for ClaEid parser.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public interface ClaEidParser {

    class UnknownClaName extends EidFormatException {
        public UnknownClaName(String claName) {
            super("unknown CL Name: " + claName);
        }
    }

    /**
     * ClaEid-specific factory.
     *
     * @param claName     convergence layer name
     * @param claSpecific convergence layer specific host-part
     * @param sink        handle may be null
     * @return new ClaEid
     * @throws EidFormatException if Convergence Layer name is unknown
     */
    ClaEid create(String claName, String claSpecific, String sink) throws EidFormatException;
}
