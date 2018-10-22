package io.left.rightmesh.libdtn.core.network;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.events.ChannelClosed;
import io.left.rightmesh.libdtn.core.events.ChannelOpened;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.utils.ReflectionUtil;
import io.left.rightmesh.libdtn.modules.cla.CLAChannel;
import io.left.rightmesh.libdtn.modules.cla.CLAInterface;
import io.left.rightmesh.librxbus.RxBus;
import io.reactivex.Single;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_CLA_LOAD_MODULES;
import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.MODULES_CLA_PATH;

/**
 * @author Lucien Loiseau on 16/10/18.
 */
public class CLAManager extends BaseComponent {

    private static final String TAG = "CLAManager";

    private DTNCore core;
    private List<CLAInterface> clas;

    public CLAManager(DTNCore core) {
        this.core = core;
        clas = new LinkedList<>();
        initComponent(core.getConf(), COMPONENT_ENABLE_CLA_LOAD_MODULES, core.getLogger());
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        loadCLAModules();
        for (CLAInterface cla : clas) {
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
                    e -> core.getLogger().w(TAG, "can't start CLA " + cla.getCLAName()+": "+e.getMessage()),
                    () -> core.getLogger().w(TAG, "CLA "+cla.getCLAName()+" has stopped"));
        }
    }

    @Override
    protected void componentDown() {
        // unload modules
    }

    private void loadCLAModules() {
        String path = core.getConf().<String>get(MODULES_CLA_PATH).value();
        try {
            File loc = new File(path);
            File[] flist = loc.listFiles(f -> f.getPath().toLowerCase().endsWith(".jar"));
            URL[] urls = new URL[flist.length];
            for (int i = 0; i < flist.length; i++) {
                urls[i] = flist[i].toURI().toURL();
            }
            URLClassLoader ucl = new URLClassLoader(urls);
            ServiceLoader<CLAInterface> sl = ServiceLoader.load(CLAInterface.class, ucl);
            for (CLAInterface cla : sl) {
                core.getLogger().i(TAG, "CLA module added: " + cla.getCLAName());
                cla.setLogger(core.getLogger());
                clas.add(cla);
            }
        } catch (MalformedURLException e) {
            System.out.println("e : " + e.getMessage());
        }
    }

    /**
     * Try to open a CLAChannel to a specific {@see EID.CLA}. The way it parses the information in
     * the EID and actually opens the channel is an implementation matter.
     */
    public Single<CLAChannel> openChannel(CLA peer) {
        for (CLAInterface cla : clas) {
            if (peer.getCLAName().equals(cla.getCLAName())) {
                return cla.open(peer);
            }
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
