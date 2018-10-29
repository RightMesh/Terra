package io.left.rightmesh.libdtn.common.data.blob;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class NullReadableBLOB implements ReadableBLOB {
    @Override
    public void read(OutputStream stream) throws IOException {
    }

    @Override
    public void close() {
    }
}
