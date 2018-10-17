package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.data.eid.EID;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local EID and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable {

    private static final String TAG = "LocalEIDTable";

    public static EID localEID() {
        return DTNConfiguration.<EID>get(DTNConfiguration.Entry.LOCAL_EID)
                .value();
    }

    public static Set<EID> aliases() {
        return DTNConfiguration.<Set<EID>>get(DTNConfiguration.Entry.ALIASES).value();
    }

    /**
     * check if an EID is local or foreign.
     *
     * @param eid
     * @return true if EID match a local EID or an alias, false otherwise
     */
    public static boolean isLocal(EID eid) {
        return matchLocal(eid) != null;
    }

    /**
     * return the matching EID.
     *
     * @param eid to check
     * @return true if the eid is local, false otherwise
     */
    public static EID matchLocal(EID eid) {
        if (eid.matches(localEID())) {
            return localEID();
        }

        for (EID alias : aliases()) {
            if (eid.matches(alias)) {
                return alias;
            }
        }
        return null;
    }
}
