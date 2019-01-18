package io.left.rightmesh.libdtn.common.utils;

/**
 * NullLogger doesn't log anything.
 *
 * @author Lucien Loiseau on 22/10/18.
 */
public class NullLogger implements Log {

    @Override
    public void v(String tag, String msg) {
    }

    @Override
    public void d(String tag, String msg) {
    }

    @Override
    public void i(String tag, String msg) {
    }

    @Override
    public void w(String tag, String msg) {
    }

    @Override
    public void e(String tag, String msg) {
    }
}
