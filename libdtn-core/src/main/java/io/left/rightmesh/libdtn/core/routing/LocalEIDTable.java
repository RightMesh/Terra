package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.LocalEIDAPI;

import java.util.Set;

/**
 * LocalEIDTable keeps track of current node local EID and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEIDTable implements LocalEIDAPI {

    private static final String TAG = "LocalEIDTable";

    private CoreAPI core;

    public LocalEIDTable(CoreAPI core) {
        this.core = core;
        core.getLogger().i(TAG, "localEID="+localEID().getEIDString());
    }

    public EID localEID() {
        return core.getConf().<EID>get(ConfigurationAPI.CoreEntry.LOCAL_EID)
                .value().copy();
    }

    public Set<EID> aliases() {
        return core.getConf().<Set<EID>>get(ConfigurationAPI.CoreEntry.ALIASES).value();
    }

    @Override
    public boolean isLocal(EID eid) {
        return matchLocal(eid) != null;
    }

    @Override
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
