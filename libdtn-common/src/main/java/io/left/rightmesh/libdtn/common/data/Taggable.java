package io.left.rightmesh.libdtn.common.data;

/**
 * A Taggable class has method to attach, remove or check attached object.
 *
 * @author Lucien Loiseau on 31/10/18.
 */
public interface Taggable {

    /**
     * tag an object.
     *
     * @param tag string
     */
    void tag(String tag);

    /**
     * tag and attach an object to this Block. It is useful for Bundle processing.
     *
     * @param key for this attachement
     * @param o   the attached object
     */
    void tag(String key, Object o);

    /**
     * remove an object attached to a tag.
     *
     * @param key key for the tag
     */
    void removeTag(String key);

    /**
     * Check wether this bundle is tagged.
     *
     * @param tag to asses
     * @return true if the bundle is tagged with this tag, false otherwise.
     */
    boolean isTagged(String tag);

    /**
     * get the object attached to a tag Not that it makes no check and is up to the
     * caller to make sure that the attached object is not null and of correct PAYLOAD_BLOCK_TYPE.
     *
     * @param key for this attachement
     * @param <T> PAYLOAD_BLOCK_TYPE of the attachement
     * @return the object attaced under this key
     */
    <T> T getTagAttachment(String key);
}
