package io.left.rightmesh.libdtn.common.data;

/**
 * MetaBundle is a Bundle that only contains the {@link PrimaryBlock}.
 *
 * @author Lucien Loiseau on 07/10/18.
 */
public class MetaBundle extends Bundle {

    public MetaBundle() {
    }

    public MetaBundle(MetaBundle meta) {
        super((PrimaryBlock)meta);
    }

    public MetaBundle(Bundle bundle) {
        super((PrimaryBlock)bundle);
    }
}
