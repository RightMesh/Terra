package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.bundleV6.EID;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local EID and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable {

    // ---- SINGLETON ----
    private static final Object lock = new Object();
    private static LocalEIDTable instance;

    private static LocalEIDTable getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new LocalEIDTable();
            }
            return instance;
        }
    }

    private EID localEID;
    private Set<EID> aliases;

    LocalEIDTable() {
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

    /**
     * check if an EID is for the local node.
     *
     * @param eid to check
     * @return true if the eid is local, false otherwise
     */
    public static boolean isLocal(EID eid) {
        if (eid.toString().startsWith(getInstance().localEID.toString())) {
            return true;
        }
        for (EID alias : getInstance().aliases) {
            if (eid.toString().startsWith(alias.toString())) {
                return true;
            }
        }
        return false;
    }
}
