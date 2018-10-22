package io.left.rightmesh.libdtn.core.network;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.modules.cla.CLAChannel;
import io.left.rightmesh.libdtn.modules.cla.CLAInterface;
import io.reactivex.Single;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_CLA_LOAD_MODULES;

/**
 * @author Lucien Loiseau on 16/10/18.
 */
public class CLAManager extends Component {

    private static final String TAG = "CLAManager";

    // ---- SINGLETON ----
    private static CLAManager instance;
    public static CLAManager getInstance() {
        return instance;
    }
    static {
        instance = new CLAManager();
        clas = new LinkedList<>();
        instance.initComponent(COMPONENT_ENABLE_CLA_LOAD_MODULES);
    }

    private static List<CLAInterface> clas;

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        ServiceLoader<CLAInterface> loader = ServiceLoader.load(CLAInterface.class);
        for (CLAInterface cla : loader) {
            System.out.println("Convergence Layer Adapter: "+cla.getCLAName());
            clas.add(cla);
        }
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        // unload modules
    }

    /**
     * Try to open a CLAChannel to a specific {@see EID.CLA}. The way it parses the information in
     * the EID and actually opens the channel is an implementation matter.
     */
    public static Single<CLAChannel> openChannel(CLA peer) {
        for(CLAInterface cla : clas) {
            if (peer.getCLAName().equals(cla.getCLAName())) {
                return cla.open(peer);
            }
        }
        return Single.error(new Throwable("no such CLA"));
    }
}
