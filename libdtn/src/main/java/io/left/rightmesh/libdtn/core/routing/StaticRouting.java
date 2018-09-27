package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_ROUTING;

/**
 * Static Routing is a routing component that uses the static route table to take
 * forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class StaticRouting extends Component {

    // ---- SINGLETON ----
    private static StaticRouting instance = new StaticRouting();
    public static StaticRouting getInstance() {  return instance; }

    private StaticRouting() {
        super(COMPONENT_ENABLE_STATIC_ROUTING);
    }

    @Override
    protected void componentUp() {

    }

    @Override
    protected void componentDown() {

    }
}
