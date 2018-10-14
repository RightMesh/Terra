package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.utils.Log;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public abstract class Component {

    private boolean enabled = false;

    public void initComponent(DTNConfiguration.Entry entry) {
        DTNConfiguration.<Boolean>get(entry).observe()
                .subscribe(
                        enabled -> {
                            this.enabled = enabled;
                            if (enabled) {
                                componentUp();
                            } else {
                                componentDown();
                            }
                        },
                        e -> {
                            /* ignore */
                        });
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void componentUp() {
        Log.i(getComponentName(), "component up");
    }

    protected void componentDown() {
        Log.i(getComponentName(), "component down");
    }

    protected abstract String getComponentName();
}
