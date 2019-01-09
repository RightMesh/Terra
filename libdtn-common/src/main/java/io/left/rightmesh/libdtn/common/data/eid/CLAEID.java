package io.left.rightmesh.libdtn.common.data.eid;

/**
 * CLAEID are a specific class of EID that maps directly to a convergence layer channel.
 * The semantic of a CLA-EID is defined in the DTN draft written by Lucien Loiseau:
 *
 * https://tools.ietf.org/html/draft-loiseau-dtn-cla-eid-00
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public interface CLAEID extends EID {

    int EID_CLA_IANA_VALUE = 3;  // not actually an IANA value (yet)
    String EID_CLA_SCHEME = "cla";

    /**
     * get the Convergence Layer name of this BaseCLAEID-EID
     *
     * @return String representing the CL Name
     */
    String getCLAName();

    /**
     * get the Convergence Layer specific part of this BaseCLAEID-EID
     *
     * @return String representing the CL-specific part
     */
    String getCLASpecificPart();


    /**
     * get the Sink part of this BaseCLAEID-EID
     *
     * @return String representing the Sink
     */
    String getPath();


}
