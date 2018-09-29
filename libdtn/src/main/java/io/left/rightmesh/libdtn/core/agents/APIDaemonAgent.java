package io.left.rightmesh.libdtn.core.agents;

import io.left.rightmesh.libdtn.core.Component;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_DAEMON_API;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class APIDaemonAgent extends Component {

    // ---- SINGLETON ----
    private static APIDaemonAgent instance = new APIDaemonAgent();
    public static APIDaemonAgent getInstance() {  return instance; }
    public static void init() {}

    APIDaemonAgent() {
        super(COMPONENT_ENABLE_DAEMON_API);
    }

}
