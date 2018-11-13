package io.left.rightmesh.libdtn.core.events;

import io.left.rightmesh.libdtn.core.spi.aa.ActiveRegistrationCallback;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationActive implements DTNEvent {
    
    public String sink;
    public ActiveRegistrationCallback cb;

    public RegistrationActive(String sink, ActiveRegistrationCallback cb) {
        this.sink = sink;
        this.cb = cb;
    }

    @Override
    public String toString() {
        return "Registration active: sink="+sink;
    }
}
