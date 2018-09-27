package io.left.rightmesh.libdtn.core.routing;

import io.left.rightmesh.libdtn.core.Component;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_API;

/**
 * Registration Routing keeps track of the registered application agent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class RegistrationTable extends Component {

    // ---- SINGLETON ----
    private static RegistrationTable instance = new RegistrationTable();
    public static RegistrationTable getInstance() {  return instance; }

    private RegistrationTable() {
        super(COMPONENT_ENABLE_API);
    }

    @Override
    protected void componentUp() {

    }

    @Override
    protected void componentDown() {

    }
}
