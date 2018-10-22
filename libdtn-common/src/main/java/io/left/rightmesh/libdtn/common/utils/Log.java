package io.left.rightmesh.libdtn.common.utils;

/**
 * Simple Logger
 *
 * @author Lucien Loiseau on 15/09/18.
 */
public interface Log {

    enum LOGLevel {
        VERBOSE("VERBOSE"),
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR");

        private String level;

        LOGLevel(String level) {
            this.level = level;
        }

        @Override
        public String toString() {
            return level;
        }
    }

    void v(String tag, String msg);

    void d(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg);
}
