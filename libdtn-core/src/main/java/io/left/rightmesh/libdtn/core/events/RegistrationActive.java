package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.routing.AARegistrar;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationActive implements DTNEvent {
    
    public String sink;
    public AARegistrar.RegistrationCallback cb;

    RegistrationActive(String sink, AARegistrar.RegistrationCallback cb) {
        this.sink = sink;
        this.cb = cb;
    }

    @Override
    public String toString() {
        return "Registration active: sink="+sink;
    }
}
