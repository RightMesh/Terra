package io.left.rightmesh.libdtn.network.cla;

import io.left.rightmesh.libdtn.data.eid.CLA;
import io.left.rightmesh.libdtn.data.eid.CLASTCP;
import io.left.rightmesh.libdtn.data.eid.EID;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 16/10/18.
 */
public class CLAManager {

    /**
     * CLAInterface Factory.
     *
     * @param claName name of the cla to create
     * @return
     */
    public static <T extends CLAInterface> Maybe<T> create(String claName) {
        if(claName.equals(STCP.getCLAName())) {
            return Maybe.just((T)new STCP());
        }
        return Maybe.empty();
    }

    /**
     * Try to open a CLAChannel to a specific {@see EID.CLA}. The way it parses the information in
     * the EID and actually opens the channel is an implementation matter.
     */
    public static Single<CLAChannel> openChannel(CLA peer) {
        if(peer.getSchemeCode().equals(EID.EIDScheme.CLASTCP)) {
            return STCP.open((CLASTCP)peer);
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
