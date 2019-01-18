package io.left.rightmesh.libdtn.core.network;

import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.core.api.CLAManagerAPI;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerAPI;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 16/10/18.
 */
public class CLAManager implements CLAManagerAPI {

    private static final String TAG = "CLAManager";

    private CoreAPI core;
    private List<ConvergenceLayerSPI> clas;

    public CLAManager(CoreAPI core) {
        this.core = core;
        clas = new LinkedList<>();
    }

    @Override
    public void addCLA(ConvergenceLayerSPI cla) {
        try {
            core.getExtensionManager().addExtensionCLA(cla.getModuleName(), cla.getCLAEIDParser());
            clas.add(cla);
            cla.start(core.getConf(), core.getLogger()).subscribe(
                    dtnChannel -> {
                        RxBus.post(new ChannelOpened(dtnChannel));
                    },
                    e -> {
                        core.getLogger().w(TAG, "can't start CLA " + cla.getModuleName() + ": " + e.getMessage());
                        clas.remove(cla);
                    },
                    () -> {
                        core.getLogger().w(TAG, "CLA " + cla.getModuleName() + " has stopped");
                        clas.remove(cla);
                    });
        } catch(ExtensionManagerAPI.CLNameAlreadyManaged cnam) {
            core.getLogger().w(TAG, "can't load CLA " + cla.getModuleName() + ": " + cnam.getMessage());
        }
    }

    @Override
    public Single<CLAChannelSPI> createOpportunity(ClaEid eid) {
        if(!core.getConf().<Boolean>get(ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE).value()) {
            return Single.error(new Throwable("AutoConnect is disabled"));
        }

        final String opp = "cla=" + eid.getClaName() + " peer=" + eid.getClaSpecificPart();
        core.getLogger().d(TAG, "trying to create an opportunity with "+opp);
        for (ConvergenceLayerSPI cla : clas) {
            if (eid.getClaName().equals(cla.getModuleName())) {
                return cla.open(eid)
                        .doOnError(e -> core.getLogger().d(TAG, "opportunity creation failed " + opp + ": "+e.getMessage()))
                        .doOnSuccess((c) -> {
                            core.getLogger().d(TAG, "opportunity creation success: " + opp);
                            RxBus.post(new ChannelOpened(c));
                        });
            }
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
