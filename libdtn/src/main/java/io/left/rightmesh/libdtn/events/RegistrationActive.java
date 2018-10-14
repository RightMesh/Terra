package io.left.rightmesh.libdtn.events;

import io.left.rightmesh.libdtn.core.routing.RegistrationTable;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationActive implements DTNEvent {
    
    public String sink;
    public RegistrationTable.RegistrationCallback cb;

    RegistrationActive(String sink, RegistrationTable.RegistrationCallback cb) {
        this.sink = sink;
        this.cb = cb;
    }
}
