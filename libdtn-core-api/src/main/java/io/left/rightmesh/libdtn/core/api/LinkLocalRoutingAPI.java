package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.reactivex.Maybe;

/**
 * @author Lucien Loiseau on 27/11/18.
 */
public interface LinkLocalRoutingAPI extends CoreComponentAPI {

    /**
     * Check if an Eid is a local link-local Eid.
     *
     * @param eid to check
     * @return the BaseClaEid-Eid matching this Eid, null otherwise.
     */
    ClaEid isEIDLinkLocal(Eid eid);

    /**
     * Find an open channel whose CLA Eid matches the Eid
     * @param destination
     * @return Maybe a CLAChannelSPI
     */
    Maybe<CLAChannelSPI> findCLA(Eid destination);

    /**
     * Dump all channel from the link local table
     * @return Set of open channel
     */
    Set<CLAChannelSPI> dumpTable();

}
