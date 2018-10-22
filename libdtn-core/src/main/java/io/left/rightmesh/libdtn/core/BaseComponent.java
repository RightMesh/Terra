package io.left.rightmesh.libdtn.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public abstract class BaseComponent implements DTNComponent {

    private boolean enabled = false;
    private static final Set<BaseComponent> registeredComponents = new HashSet<>();

    public void initComponent(DTNConfiguration conf, DTNConfiguration.Entry entry) {
        registeredComponents.add(this);
        conf.<Boolean>get(entry).observe()
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

    public static Collection<BaseComponent> getAllComponents() {
        return Collections.unmodifiableCollection(registeredComponents);
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected abstract void componentUp();

    protected abstract void componentDown();

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
        if(o instanceof BaseComponent) {
            return this.getComponentName().equals(((BaseComponent)o).getComponentName());
        } else {
            return false;
        }
    }
}
