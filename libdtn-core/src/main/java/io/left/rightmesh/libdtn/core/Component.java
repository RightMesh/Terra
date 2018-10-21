package io.left.rightmesh.libdtn.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.utils.Log;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public abstract class Component {

    private boolean enabled = false;
    private static final Set<Component> registeredComponents = new HashSet<>();

    public void initComponent(DTNConfiguration.Entry entry) {
        registeredComponents.add(this);
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

    public static Collection<Component> getAllComponents() {
        return Collections.unmodifiableCollection(registeredComponents);
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

    public abstract String getComponentName();

    @Override
    public int hashCode() {
        return getComponentName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o instanceof Component) {
            return this.getComponentName().equals(((Component)o).getComponentName());
        } else {
            return false;
        }
    }
}
