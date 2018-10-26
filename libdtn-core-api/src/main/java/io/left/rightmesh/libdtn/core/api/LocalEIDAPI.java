package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * @author Lucien Loiseau on 26/10/18.
 */
public interface LocalEIDAPI {

    EID localEID();

    Set<EID> aliases();

    boolean isLocal(EID eid);

    EID matchLocal(EID eid);

}
