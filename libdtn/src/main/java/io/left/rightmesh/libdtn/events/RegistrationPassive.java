package io.left.rightmesh.libdtn.events;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationPassive implements DTNEvent {
    public String sink;

    RegistrationPassive(String sink) {
        this.sink = sink;
    }
}
