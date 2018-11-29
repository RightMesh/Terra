package io.left.rightmesh.libdtn.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.api.ConfigurationAPI;
import io.left.rightmesh.libdtn.core.api.CoreComponentAPI;

/**
 * @author Lucien Loiseau on 27/09/18.
 */
public abstract class CoreComponent implements CoreComponentAPI {

    private boolean enabled = false;
    private static final Set<CoreComponent> registeredComponents = new HashSet<>();

    @Override
    public void initComponent(ConfigurationAPI conf, ConfigurationAPI.CoreEntry entry, Log logger) {
        registeredComponents.add(this);
        conf.<Boolean>get(entry).observe()
                .subscribe(
                        enabled -> {
                            this.enabled = enabled;
                            if (enabled) {
                                if(logger != null) {
                                    logger.i(getComponentName(), "UP");
                                }
                                componentUp();
                            } else {
                                if(logger != null) {
                                    logger.i(getComponentName(), "DOWN");
                                }
                                componentDown();
                            }
                        },
                        e -> {
                            /* ignore */
                        });
    }

    public static Collection<CoreComponent> getAllComponents() {
        return Collections.unmodifiableCollection(registeredComponents);
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected abstract void componentUp();

    protected abstract void componentDown();

    @Override
    public int hashCode() {
        return getComponentName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o instanceof CoreComponent) {
            return this.getComponentName().equals(((CoreComponent)o).getComponentName());
        } else {
            return false;
        }
    }
}
