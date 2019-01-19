package io.left.rightmesh.libdtn.core.network;

import io.left.rightmesh.libdtn.common.data.eid.ClaEid;
import io.left.rightmesh.libdtn.core.api.ClaManagerApi;
import io.left.rightmesh.libdtn.core.api.ConfigurationApi;
import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.ExtensionManagerApi;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.spi.cla.ClaChannelSpi;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSpi;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Single;

import java.util.LinkedList;
import java.util.List;

/**
 * ClaManager implements the ClaManagerApi and provides entry point to add convergence layers.
 *
 * @author Lucien Loiseau on 16/10/18.
 */
public class ClaManager implements ClaManagerApi {

    private static final String TAG = "ClaManager";

    private CoreApi core;
    private List<ConvergenceLayerSpi> clas;

    public ClaManager(CoreApi core) {
        this.core = core;
        clas = new LinkedList<>();
    }

    @Override
    public void addCla(ConvergenceLayerSpi cla) {
        try {
            core.getExtensionManager().addExtensionCla(cla.getModuleName(), cla.getClaEidParser());
            clas.add(cla);
            cla.start(core.getConf(), core.getLogger()).subscribe(
                    dtnChannel -> {
                        RxBus.post(new ChannelOpened(dtnChannel));
                    },
                    e -> {
                        core.getLogger().w(TAG, "can't start CLA " + cla.getModuleName()
                                + ": " + e.getMessage());
                        clas.remove(cla);
                    },
                    () -> {
                        core.getLogger().w(TAG, "CLA " + cla.getModuleName() + " has stopped");
                        clas.remove(cla);
                    });
        } catch (ExtensionManagerApi.ClaNameAlreadyManaged cnam) {
            core.getLogger().w(TAG, "can't load CLA " + cla.getModuleName() + ": "
                    + cnam.getMessage());
        }
    }

    @Override
    public Single<ClaChannelSpi> createOpportunity(ClaEid eid) {
        if (!core.getConf().<Boolean>get(ConfigurationApi.CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE)
                .value()) {
            return Single.error(new Throwable("AutoConnect is disabled"));
        }

        final String opp = "cla=" + eid.getClaName() + " peer=" + eid.getClaSpecificPart();
        core.getLogger().d(TAG, "trying to create an opportunity with " + opp);
        for (ConvergenceLayerSpi cla : clas) {
            if (eid.getClaName().equals(cla.getModuleName())) {
                return cla.open(eid)
                        .doOnError(e -> core.getLogger().d(TAG, "opportunity creation failed "
                                + opp + ": " + e.getMessage()))
                        .doOnSuccess((c) -> {
                            core.getLogger().d(TAG, "opportunity creation success: " + opp);
                            RxBus.post(new ChannelOpened(c));
                        });
            }
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
