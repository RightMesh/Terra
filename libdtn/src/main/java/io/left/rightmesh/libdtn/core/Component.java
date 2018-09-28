package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.DTNConfiguration;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public abstract class Component {

    private boolean enabled = false;

    protected Component(DTNConfiguration.Entry entry) {
        DTNConfiguration.<Boolean>get(entry).observe()
                .subscribe(enabled -> {
                    this.enabled = enabled;
                    if(enabled) {
                        componentUp();
                    } else {
                        componentDown();
                    }
                });
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void componentUp() {
    }

    protected void componentDown() {
    }
}
