package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.EID;
import io.left.rightmesh.libdtn.utils.Log;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local EID and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable {

    private static final String TAG = "LocalEIDTable";

    // ---- SINGLETON ----
    private static LocalEIDTable instance;
    public static LocalEIDTable getInstance() {  return instance; }

    static {
        instance = new LocalEIDTable();
        Log.i(TAG, "component up");
    }

    private EID localEID;
    private Set<EID> aliases;

    private LocalEIDTable() {
        DTNConfiguration.<EID>get(DTNConfiguration.Entry.LOCAL_EID)
                .observe()
                .subscribe(
                        eid -> {
                            this.localEID = eid;
                        });
        DTNConfiguration.<Set<EID>>get(DTNConfiguration.Entry.ALIASES)
                .observe()
                .subscribe(
                        s -> {
                            this.aliases = s;
                        });
    }

    public static EID localEID() {
        return getInstance().localEID;
    }

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
        if (eid.matches(getInstance().localEID)) {
            return localEID();
        }

        for (EID alias : getInstance().aliases) {
            if (eid.matches(alias)) {
                return alias;
            }
        }
        return null;
    }
}
