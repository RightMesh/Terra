package io.left.rightmesh.libdtn.common.utils;

/**
 * @author Lucien Loiseau on 30/10/18.
 */
public class SimpleLogger implements Log {

    private LOGLevel level = LOGLevel.VERBOSE;

    private void log(LOGLevel l, String tag, String msg) {
        if (l.ordinal() >= level.ordinal()) {
            System.out.println(System.currentTimeMillis() + " " + l + " - " + tag + ": " + msg);
        }
    }

    public void set(LOGLevel level) {
        this.level = level;
    }

    @Override
    public void v(String tag, String msg) {
        log(LOGLevel.VERBOSE, tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        log(LOGLevel.DEBUG, tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        log(LOGLevel.INFO, tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        log(LOGLevel.WARN, tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        log(LOGLevel.ERROR, tag, msg);
    }
}
