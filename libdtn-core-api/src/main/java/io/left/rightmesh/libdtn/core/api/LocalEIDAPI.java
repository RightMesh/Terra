package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface LocalEIDAPI {

    /**
     * Return the configured local EID for current node.
     *
     * @return local EID
     */
    EID localEID();

    /**
     * Return the set of all aliases for current node.
     *
     * @return Set of EID
     */
    Set<EID> aliases();

    /**
     * check if an EID is local or foreign.
     *
     * @param eid to check
     * @return true if EID match a local EID or an alias, false otherwise
     */
    boolean isLocal(EID eid);

    /**
     * return the matching EID.
     *
     * @param eid to check
     * @return the EID that matches this EID, null otherwise
     */
    EID matchLocal(EID eid);

}
