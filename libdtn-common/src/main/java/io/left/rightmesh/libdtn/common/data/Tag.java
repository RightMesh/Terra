package io.left.rightmesh.libdtn.common.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lucien Loiseau on 31/10/18.
 */
public abstract class Tag implements Taggable {

    private Map<String, Object> attachement;

    public Tag() {
        attachement = new HashMap<>();
    }

    public void tag(String tag) {
        tag(tag, null);
    }

    public void tag(String key, Object o) {
        attachement.putIfAbsent(key, o);
    }

    public void removeTag(String key) {
        attachement.remove(key);
    }

    public boolean isTagged(String tag) {
        return attachement.containsKey(tag);
    }

    public <T> T getTagAttachment(String key) {
        return (T) attachement.get(key);
    }


}
