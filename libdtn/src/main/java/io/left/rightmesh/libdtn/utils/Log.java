package io.left.rightmesh.libdtn.utils;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_LOGGING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.LOG_LEVEL;

/**
 * Simple Logger
 *
 * @author Lucien Loiseau on 15/09/18.
 */
public class Log extends Component {

    private static final String TAG = "Log";

    // ---- SINGLETON ----
    private static Log instance;

    public static Log getInstance() {
        return instance;
    }

    static {
        instance = new Log();
        level = LOGLevel.INFO;
        getInstance().initComponent(COMPONENT_ENABLE_LOGGING);
        DTNConfiguration.<LOGLevel>get(LOG_LEVEL).observe().subscribe(l -> level = l);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        super.componentDown();
        RxBus.unregister(this);
    }

    public enum LOGLevel {
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
    private static LOGLevel level;

    private static void log(LOGLevel l, String tag, String msg) {
        if (getInstance().isEnabled()) {
            if (l.ordinal() >= level.ordinal()) {
                System.out.println(l + " - " + tag + ": " + msg);
            }
        }
    }

    public static void set(LOGLevel level) { Log.level = level; }

    public static void v(String tag, String msg) {
        log(LOGLevel.VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        log(LOGLevel.DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        log(LOGLevel.INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        log(LOGLevel.WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        log(LOGLevel.ERROR, tag, msg);
    }

    @Subscribe
    public void onEvent(Object o) {
        Log.d(TAG, "EventReceived - "+o.toString());
    }

}
