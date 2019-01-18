package io.left.rightmesh.libdtn.common.utils;

/**
 * Simple Logger.
 *
 * @author Lucien Loiseau on 15/09/18.
 */
public interface Log {

    enum LogLevel {
        VERBOSE("VERBOSE"),
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR");

        private String level;

        LogLevel(String level) {
            this.level = level;
        }

        @Override
        public String toString() {
            return level;
        }
    }

    // CHECKSTYLE IGNORE MethodName
    void v(String tag, String msg);

    void d(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg);
    //CHECKSTYLE END IGNORE
}
