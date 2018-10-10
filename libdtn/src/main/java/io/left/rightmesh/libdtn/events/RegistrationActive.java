package io.left.rightmesh.libdtn.events;

/**
 * @author Lucien Loiseau on 10/10/18.
 */
public class RegistrationActive implements DTNEvent {

    public static final String id = "ChannelClosed";

    public String sink;

    RegistrationActive(String sink) {
        this.sink = sink;
    }

    @Override
    public String getID() {
        return id;
    }
}
