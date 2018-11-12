package io.left.rightmesh.libdtn.common.utils;

/**
 * @author Lucien Loiseau on 30/10/18.
 */
public class SimpleLogger implements Log {

    @Override
    public void v(String tag, String msg) {
        System.out.println(System.currentTimeMillis() + " " + tag + " " + msg);
    }

    @Override
    public void d(String tag, String msg) {
        System.out.println(System.currentTimeMillis() + " " + tag + " " + msg);
    }

    @Override
    public void i(String tag, String msg) {
        System.out.println(System.currentTimeMillis() + " " + tag + " " + msg);
    }

    @Override
    public void w(String tag, String msg) {
        System.out.println(System.currentTimeMillis() + " " + tag + " " + msg);
    }

    @Override
    public void e(String tag, String msg) {
        System.out.println(System.currentTimeMillis() + " " + tag + " " + msg);
    }

}
