package io.left.rightmesh.module.core.ipdiscovery;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.common.data.eid.CLAEID;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;


/**
 * @author Lucien Loiseau on 27/11/18.
 */
public class CoreModuleIpDiscovery implements CoreModuleSPI {

    private static final String TAG = "IpDiscovery";

    private CoreAPI core;

    @Override
    public String getModuleName() {
        return "ipdiscovery";
    }

    @Override
    public void init(CoreAPI api) {
        this.core = api;
        LibDetect.start(4000, new ActionListener() {
            @Override
            public void onPeerReachable(PeerReachable peer) {
                api.getLogger().i(TAG, "peer detected :" + peer.address.getHostAddress());
                if (core.getConf().<Boolean>get(ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT).value()) {
                    try {
                        EID eid = api.getExtensionManager().getEIDFactory().create(
                                "cla:stcp:" + peer.address.getHostAddress() + ":" + 4556 + "/");
                        core.getClaManager().createOpportunity((CLAEID) eid).subscribe(
                                channel -> {
                                    // ignore
                                },
                                e -> {
                                    // ignore
                                }
                        );
                    } catch (EIDFormatException e) {
                        // the stcp module is not up, so we cannot talk to a ip neighbour
                    }
                }
            }

            @Override
            public void onPeerUnreachable(PeerUnreachable peer) {
                api.getLogger().i(TAG, "peer unreachable");
            }
        }, true);
    }

}