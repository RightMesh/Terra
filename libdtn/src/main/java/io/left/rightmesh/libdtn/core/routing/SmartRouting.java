package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_SMART_ROUTING;

/**
 * SmartRouting is a routing component that uses a byte-code block to take forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class SmartRouting extends Component {

    // ---- SINGLETON ----
    private static SmartRouting instance = new SmartRouting();
    public static SmartRouting getInstance() {  return instance; }
    public static void init() {}

    private SmartRouting() {
        super(COMPONENT_ENABLE_SMART_ROUTING);
    }

}
