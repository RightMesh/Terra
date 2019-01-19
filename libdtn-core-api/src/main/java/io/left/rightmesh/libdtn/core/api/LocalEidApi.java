package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.util.Set;

/**
 * API to request the local Eid of the node.
 *
 * @author Lucien Loiseau on 26/10/18.
 */
public interface LocalEidApi {

    /**
     * Return the configured local Eid for current node.
     *
     * @return local Eid
     */
    Eid localEid();

    /**
     * Return the set of all aliases for current node.
     *
     * @return Set of Eid
     */
    Set<Eid> aliases();

    /**
     * check if an Eid is local or foreign.
     *
     * @param eid to check
     * @return true if Eid match a local Eid or an alias, false otherwise
     */
    boolean isLocal(Eid eid);

    /**
     * return the matching Eid.
     *
     * @param eid to check
     * @return the Eid that matches this Eid, null otherwise
     */
    Eid matchLocal(Eid eid);

}
