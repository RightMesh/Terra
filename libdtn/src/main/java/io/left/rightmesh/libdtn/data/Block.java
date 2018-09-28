package io.left.rightmesh.libdtn.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lucien Loiseau on 28/09/18.
 */
public class Block {

    // processing 
    public Map<String, Object> attachement;

    Block() {
        attachement = new HashMap<>();
    }

    /**
     * Add a TAG on this Bundle. It is useful for Bundle processing.
     *
     * @param tag to add to this bundle
     * @return true if the tag was added, false if the bundle was already tagged with this tag.
     */
    public boolean mark(String tag) {
        return attach(tag, null);
    }

    /**
     * Check wether this bundle is tagged.
     *
     * @param tag to asses
     * @return true if the bundle is tagged with this tag, false otherwise.
     */
    public boolean isMarked(String tag) {
        return attachement.containsKey(tag);
    }

    /**
     * attach an object to this bundle. It is useful for Bundle processing.
     *
     * @param key for this attachement
     * @param o   the attached object
     * @return false if there was already an object attached under this key, true otherwise.
     */
    public boolean attach(String key, Object o) {
        if (attachement.containsKey(key)) {
            return false;
        }
        attachement.put(key, o);
        return true;
    }

    /**
     * get the attachement under this key.
     *
     * @param key for this attachement
     * @param <T> type of the attachement
     * @return the attached object under this key
     */
    public <T> T getAttachement(String key) {
        if (attachement.containsKey(key)) {
            return (T) attachement.get(key);
        }
        return null;
    }

}
