package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_SMART_ROUTING;

/**
 * SmartRouting is a routing component that uses a byte-code block to take forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class SmartRouting extends Component {

    private static final String TAG = "SmartRouting";

    // ---- SINGLETON ----
    private static SmartRouting instance;
    public static SmartRouting getInstance() {  return instance; }

    static {
        instance = new SmartRouting();
        getInstance().initComponent(COMPONENT_ENABLE_SMART_ROUTING);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }
}
