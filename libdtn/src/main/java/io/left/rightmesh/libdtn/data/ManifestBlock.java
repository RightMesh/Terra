package io.left.rightmesh.libdtn.data;

/**
 * @author Lucien Loiseau on 17/09/18.
 */
public class ManifestBlock extends CanonicalBlock {

    public static final int type = 4;

    public ManifestBlock() {
        super(type);
    }

}
