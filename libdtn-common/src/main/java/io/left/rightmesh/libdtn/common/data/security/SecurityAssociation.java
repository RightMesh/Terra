package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

/**
 * SecurityAssociation describes a cipher as described in BPSec.
 *
 * @author Lucien Loiseau on 03/11/18.
 */
public class SecurityAssociation {

    public enum SecurityAssociationFlags {
        EID_SCOPE,
        BLOCK_TYPE_SCOPE,
        CIPHER_SUITE_ID_PRESENT,
        SECURITY_SOURCE_PRESENT,
        SECURITY_ASSOCIATION_PARAMETER_PRESENT
    }

    int securityAssociationId;
    int securityAssociationFlag;
    Eid[] eidScope;
    int[] blockTypeScope;
    int cipherSuiteId;
    Eid securitySource;
    SecurityAssociationParameter[] securityAssociationParameters;


    /**
     * Get the state of a specific {@link SecurityAssociation.SecurityAssociationFlags}.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    public boolean getSaFlag(SecurityAssociationFlags flag) {
        return ((securityAssociationFlag >> flag.ordinal()) & 0x1) == 0x1;
    }

    /**
     * Set/clear a flag on this BlockHeader.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    public void setSaFlag(SecurityAssociationFlags flag, boolean state) {
        if (state) {
            securityAssociationFlag |= (1 << flag.ordinal());
        } else {
            securityAssociationFlag &= ~(1 << flag.ordinal());
        }
    }

    /**
     * set the security source for this Security Block.
     *
     * @param source Eid of the security source
     */
    public void setSecuritySource(Eid source) {
        this.securitySource = source;
        setSaFlag(SecurityAssociationFlags.SECURITY_SOURCE_PRESENT, true);
    }

}
