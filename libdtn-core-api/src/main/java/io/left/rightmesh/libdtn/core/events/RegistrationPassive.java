package io.left.rightmesh.libdtn.core.events;

/**
 * RegistrationPassive event is thrown whenever an application-agent is passive.
 *
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationPassive implements DtnEvent {
    public String sink;

    RegistrationPassive(String sink) {
        this.sink = sink;
    }


    @Override
    public String toString() {
        return "Registration passive: sink=" + sink;
    }
}
