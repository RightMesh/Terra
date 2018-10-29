package io.left.rightmesh.libdtn.common.data.blob;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Lucien Loiseau on 29/10/18.
 */
public class NullWritableBLOB implements WritableBLOB {
    @Override
    public void clear() {
    }

    @Override
    public int write(InputStream stream) {
        return 0;
    }

    @Override
    public int write(InputStream stream, int size) {
        return 0;
    }

    @Override
    public int write(byte b) {
        return 0;
    }

    @Override
    public int write(byte[] a) {
        return 0;
    }

    @Override
    public int write(ByteBuffer buffer) {
        return 0;
    }

    @Override
    public void close() {

    }
}
