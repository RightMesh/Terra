package io.left.rightmesh.libdtn.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public abstract class Block {

    // processing
    private Map<String, Object> attachement;

    Block() {
        attachement = new ConcurrentHashMap<>();
    }

    /**
     * Add a TAG to this Block. It is useful for Bundle processing.
     *
     * @param tag to add to this bundle
     * @return true if the tag was added, false if the bundle was already tagged with this tag.
     */
    public boolean tag(String tag) {
        return tag(tag, null);
    }

    /**
     * tag and attach an object to this Block. It is useful for Bundle processing.
     *
     * @param key for this attachement
     * @param o   the attached object
     * @return false if there was already an object attached under this key, true otherwise.
     */
    public boolean tag(String key, Object o) {
        if (attachement.containsKey(key)) {
            return false;
        }
        attachement.put(key, o);
        return true;
    }

    public void removeTag(String key) {
        attachement.remove(key);
    }

    /**
     * Check wether this bundle is tagged.
     *
     * @param tag to asses
     * @return true if the bundle is tagged with this tag, false otherwise.
     */
    public boolean isTagged(String tag) {
        return attachement.containsKey(tag);
    }

    /**
     * get the object attached to a tag Not that it makes no check and is up to the
     * caller to make sure that the attached object is not null and of correct type.
     *
     * @param key for this attachement
     * @param <T> type of the attachement
     * @return the object attaced under this key
     */
    public <T> T getTagAttachment(String key) {
        if (attachement.containsKey(key)) {
            return (T) attachement.get(key);
        }
        return null;
    }

}
