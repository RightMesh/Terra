package io.left.rightmesh.libdtn.core.processor;

import java.util.HashMap;
import java.util.Map;

import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.BundleID;
import io.left.rightmesh.libdtn.events.DTNEvent;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_EVENT_PROCESSING;

/**
 * @author Lucien Loiseau on 14/10/18.
 */
public class EventProcessor extends Component {

    public static final String TAG = "EventProcessor";

    // ---- SINGLETON ----
    private static EventProcessor instance = new EventProcessor();
    public static EventProcessor getInstance() {
        return instance;
    }
    public static void init() {
        getInstance().initComponent(COMPONENT_ENABLE_EVENT_PROCESSING);
    }

    // ---- Component Specific Override----
    @Override
    protected String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        super.componentUp();
        eventMap = new HashMap<>();
    }

    @Override
    protected void componentDown() {
        super.componentDown();
    }

    static Map<DTNEvent, EventListener> eventMap;


    public abstract class EventListener<T> {

        Map<T, BundleID> watchList;

        EventListener() {
            watchList = new HashMap<>();
        }

        public void watch(T key, BundleID bid) {
        }
    }

}
