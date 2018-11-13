package io.left.rightmesh.libdtn.core.events;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationPassive implements DTNEvent {
    public String sink;

    RegistrationPassive(String sink) {
        this.sink = sink;
    }


    @Override
    public String toString() {
        return "Registration passive: sink="+sink;
    }
}
