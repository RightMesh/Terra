package io.left.rightmesh.libdtn.common.data;

/**
 * ManifestBlock todo.
 *
 * @author Lucien Loiseau on 17/09/18.
 */
public class ManifestBlock extends CanonicalBlock {

    public static final int MANIFEST_BLOCK_TYPE = 4;

    /**
     * Constructor.
     */
    public ManifestBlock() {
        super(MANIFEST_BLOCK_TYPE);
    }

}
