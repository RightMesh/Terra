package io.left.rightmesh.module.core.ipdiscovery;

import io.left.rightmesh.libdetect.ActionListener;
import io.left.rightmesh.libdetect.LibDetect;
import io.left.rightmesh.libdetect.PeerReachable;
import io.left.rightmesh.libdetect.PeerUnreachable;
import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.left.rightmesh.libdtn.common.data.eid.EidFormatException;
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
                        Eid eid = api.getExtensionManager().getEidFactory().create(
                                "cla:stcp:" + peer.address.getHostAddress() + ":" + 4556 + "/");
                        core.getClaManager().createOpportunity((ClaEid) eid).subscribe(
                                channel -> {
                                    // ignore
                                },
                                e -> {
                                    // ignore
                                }
                        );
                    } catch (EidFormatException e) {
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