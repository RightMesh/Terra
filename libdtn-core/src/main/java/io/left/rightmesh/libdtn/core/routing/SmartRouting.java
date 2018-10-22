package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.libdtn.core.DTNCore;

import static io.left.rightmesh.libdtn.core.DTNConfiguration.Entry.COMPONENT_ENABLE_SMART_ROUTING;

/**
 * SmartRouting is a routing component that uses a byte-code block to take forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class SmartRouting extends BaseComponent {

    private static final String TAG = "SmartRouting";

    public SmartRouting(DTNCore core) {
        initComponent(core.getConf(), COMPONENT_ENABLE_SMART_ROUTING);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {

    }

    @Override
    protected void componentDown() {

    }
}
