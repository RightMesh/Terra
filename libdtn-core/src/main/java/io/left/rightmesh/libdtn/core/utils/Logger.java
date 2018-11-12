package io.left.rightmesh.libdtn.core.utils;

import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.core.BaseComponent;
import io.left.rightmesh.librxbus.RxBus;
import io.left.rightmesh.librxbus.Subscribe;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_LOGGING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.LOG_LEVEL;

/**
 * Simple Logger
 *
 * @author Lucien Loiseau on 15/09/18.
 */
public class Logger extends BaseComponent implements Log {

    private static final String TAG = "Logger";

    public Logger(DTNConfiguration conf) {
        level = LOGLevel.INFO;
        conf.<LOGLevel>get(LOG_LEVEL).observe().subscribe(l -> level = l);
        initComponent(conf, COMPONENT_ENABLE_LOGGING, null);
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        RxBus.register(this);
    }

    @Override
    protected void componentDown() {
        RxBus.unregister(this);
    }

    private static LOGLevel level;

    private void log(LOGLevel l, String tag, String msg) {
        if (isEnabled()) {
            if (l.ordinal() >= level.ordinal()) {
                System.out.println(System.currentTimeMillis()+" "+l + " - " + tag + ": " + msg);
            }
        }
    }

    public void set(LOGLevel level) { Logger.level = level; }

    public void v(String tag, String msg) {
        log(LOGLevel.VERBOSE, tag, msg);
    }

    public void d(String tag, String msg) {
        log(LOGLevel.DEBUG, tag, msg);
    }

    public void i(String tag, String msg) {
        log(LOGLevel.INFO, tag, msg);
    }

    public void w(String tag, String msg) {
        log(LOGLevel.WARN, tag, msg);
    }

    public void e(String tag, String msg) {
        log(LOGLevel.ERROR, tag, msg);
    }

    @Subscribe
    public void onEvent(Object o) {
        d(TAG, "EventReceived - "+o.toString());
    }

}
