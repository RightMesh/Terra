package io.left.rightmesh.libdtn.events;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationPassive implements DTNEvent {

    public static final String id = "RegistrationPassive";

    public String sink;

    RegistrationPassive(String sink) {
        this.sink = sink;
    }

    @Override
    public String getID() {
        return id;
    }
}
