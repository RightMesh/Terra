package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.DTNCore;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local EID and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable {

    private static final String TAG = "LocalEIDTable";

    DTNCore core;

    public LocalEIDTable(DTNCore core) {
        this.core = core;
    }

    public EID localEID() {
        return core.getConf().<EID>get(DTNConfiguration.Entry.LOCAL_EID)
                .value();
    }

    public Set<EID> aliases() {
        return core.getConf().<Set<EID>>get(DTNConfiguration.Entry.ALIASES).value();
    }

    /**
     * check if an EID is local or foreign.
     *
     * @param eid
     * @return true if EID match a local EID or an alias, false otherwise
     */
    public boolean isLocal(EID eid) {
        return matchLocal(eid) != null;
    }

    /**
     * return the matching EID.
     *
     * @param eid to check
     * @return true if the eid is local, false otherwise
     */
    public EID matchLocal(EID eid) {
        if (eid.matches(localEID())) {
            return localEID();
        }

        for (EID alias : aliases()) {
            if (eid.matches(alias)) {
                return alias;
            }
        }

        return core.getLinkLocalRouting().isEIDLinkLocal(eid);
    }
}
