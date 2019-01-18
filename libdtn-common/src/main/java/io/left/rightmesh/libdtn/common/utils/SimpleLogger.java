package io.left.rightmesh.libdtn.common.utils;

/**
 * SimpleLogger prints the log messages on the standard output.
 *
 * @author Lucien Loiseau on 30/10/18.
 */
public class SimpleLogger implements Log {

    private LogLevel level = LogLevel.VERBOSE;

    private void log(LogLevel l, String tag, String msg) {
        if (l.ordinal() >= level.ordinal()) {
            System.out.println(System.currentTimeMillis() + " " + l + " - " + tag + ": " + msg);
        }
    }

    public void set(LogLevel level) {
        this.level = level;
    }

    @Override
    public void v(String tag, String msg) {
        log(LogLevel.VERBOSE, tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        log(LogLevel.DEBUG, tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        log(LogLevel.INFO, tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        log(LogLevel.WARN, tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        log(LogLevel.ERROR, tag, msg);
    }
}
