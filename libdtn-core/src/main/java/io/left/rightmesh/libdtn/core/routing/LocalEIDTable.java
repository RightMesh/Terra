package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.LocalEIDAPI;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local Eid and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable implements LocalEIDAPI {

    private static final String TAG = "LocalEIDTable";

    private CoreAPI core;

    public LocalEIDTable(CoreAPI core) {
        this.core = core;
        core.getLogger().i(TAG, "localEID="+localEID().getEidString());
    }

    public Eid localEID() {
        return core.getConf().<Eid>get(ConfigurationAPI.CoreEntry.LOCAL_EID)
                .value().copy();
    }

    public Set<Eid> aliases() {
        return core.getConf().<Set<Eid>>get(ConfigurationAPI.CoreEntry.ALIASES).value();
    }

    @Override
    public boolean isLocal(Eid eid) {
        return matchLocal(eid) != null;
    }

    @Override
    public Eid matchLocal(Eid eid) {
        if (eid.matches(localEID())) {
            return localEID();
        }
        for (Eid alias : aliases()) {
            if (eid.matches(alias)) {
                return alias;
            }
        }
        return core.getLinkLocalRouting().isEIDLinkLocal(eid);
    }
}
