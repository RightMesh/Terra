package io.left.rightmesh.libdtn.core.network;

import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.spi.cla.CLAChannelSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Single;

/**
 * @author Lucien Loiseau on 16/10/18.
 */
public class CLAManager {

    private static final String TAG = "CLAManager";

    private DTNCore core;
    private List<ConvergenceLayerSPI> clas;

    public CLAManager(DTNCore core) {
        this.core = core;
        clas = new LinkedList<>();
    }


    public void addCLA(ConvergenceLayerSPI cla) {
        clas.add(cla);
        cla.start().subscribe(
                dtnChannel -> {
                    RxBus.post(new ChannelOpened(dtnChannel));
                    dtnChannel.recvBundle().subscribe(
                            b -> {
                                core.getLogger().i(TAG, dtnChannel.channelEID().getEIDString() + " -> received a new bundle from: " + b.source.getEIDString() + " to: " + b.destination.getEIDString());
                                core.getBundleProcessor().bundleReception(b);
                            },
                            e -> {
                                // channel has closed
                                RxBus.post(new ChannelClosed(dtnChannel));
                            },
                            () -> {
                                // channel has closed
                                RxBus.post(new ChannelClosed(dtnChannel));
                            }
                    );
                },
                e -> core.getLogger().w(TAG, "can't start CLA " + cla.getModuleName() + ": " + e.getMessage()),
                () -> core.getLogger().w(TAG, "CLA " + cla.getModuleName() + " has stopped"));
    }


    public Single<CLAChannelSPI> openChannel(CLA peer) {
        for (ConvergenceLayerSPI cla : clas) {
            if (peer.getCLAName().equals(cla.getModuleName())) {
                return cla.open(peer);
            }
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
