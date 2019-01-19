package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.LocalEidApi;

import java.util.Set;

/**
 * LocalEidTable keeps track of current node local Eid and aliases.
 *
 * @author Lucien Loiseau on 04/09/18.
 */
public class LocalEidTable implements LocalEidApi {

    private static final String TAG = "LocalEidTable";

    private CoreApi core;

    public LocalEidTable(CoreApi core) {
        this.core = core;
        core.getLogger().i(TAG, "localEid=" + localEid().getEidString());
    }

    public Eid localEid() {
        return core.getConf().<Eid>get(ConfigurationApi.CoreEntry.LOCAL_EID)
                .value().copy();
    }

    public Set<Eid> aliases() {
        return core.getConf().<Set<Eid>>get(ConfigurationApi.CoreEntry.ALIASES).value();
    }

    @Override
    public boolean isLocal(Eid eid) {
        return matchLocal(eid) != null;
    }

    @Override
    public Eid matchLocal(Eid eid) {
        if (eid.matches(localEid())) {
            return localEid();
        }
        for (Eid alias : aliases()) {
            if (eid.matches(alias)) {
                return alias;
            }
        }
        return core.getLinkLocalRouting().isEidLinkLocal(eid);
    }
}
