package io.left.rightmesh.libdtn.core.routing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.left.rightmesh.libdtn.DTNConfiguration;
import io.left.rightmesh.libdtn.core.Component;
import io.left.rightmesh.libdtn.data.EID;
import io.reactivex.Observable;

import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.COMPONENT_ENABLE_STATIC_ROUTING;
import static io.left.rightmesh.libdtn.DTNConfiguration.Entry.STATIC_ROUTE_CONFIGURATION;
import static io.left.rightmesh.libdtn.data.EID.EIDScheme.CLA;

/**
 * Static Routing is a routing component that uses the static route table to take
 * forwarding decisions.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class RoutingTable extends Component {

    private static final String TAG = "RoutingTable";

    // ---- SINGLETON ----
    private static RoutingTable instance;

    public static RoutingTable getInstance() {
        return instance;
    }


    // ---- RoutingTable ----
    private static Set<TableEntry> staticRoutingTable;
    private static Set<TableEntry> routingTable;

    static {
        instance = new RoutingTable();
        staticRoutingTable = new HashSet<>();
        routingTable = new HashSet<>();
        instance.initComponent(COMPONENT_ENABLE_STATIC_ROUTING);
        DTNConfiguration.<Map<EID, EID>>get(STATIC_ROUTE_CONFIGURATION).observe().subscribe(
                m -> {
                    staticRoutingTable.clear();
                    m.forEach((to, from) -> staticRoutingTable.add(new TableEntry(to, from)));
                });
    }


    private static class TableEntry {
        EID to;
        EID next;

        TableEntry(EID to, EID next) {
            this.to = to;
            this.next = next;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof TableEntry) {
                return next.equals(o) && to.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return next.toString().concat(to.toString()).hashCode();
        }
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    static Observable<TableEntry> compoundTableObservable() {
        return Observable.fromIterable(staticRoutingTable)
                .concatWith(Observable.fromIterable(routingTable));
    }


    static Observable<EID> lookupPotentialNextHops(EID destination) {
        return Observable.concat(Observable.just(destination)
                        .filter(eid -> destination instanceof EID.CLA),
                compoundTableObservable()
                        .filter(entry -> destination.matches(entry.to))
                        .map(entry -> entry.next));
    }

    static Observable<EID.CLA> resolveEID(EID destination) {
        return resolveEID(destination, Observable.empty());
    }

    private static Observable<EID.CLA> resolveEID(EID destination, Observable<EID> path) {
        if (!getInstance().isEnabled()) {
            return Observable.empty();
        }

        return Observable.concat(
                lookupPotentialNextHops(destination)
                        .filter(eid -> eid.getScheme().equals(CLA))
                        .map(eid -> (EID.CLA) eid),
                lookupPotentialNextHops(destination)
                        .filter(eid -> !eid.getScheme().equals(CLA))
                        .flatMap(candidate ->
                                path.contains(candidate)
                                        .toObservable()
                                        .flatMap((b) -> {
                                            if (!b) {
                                                return resolveEID(
                                                        candidate,
                                                        path.concatWith(Observable.just(candidate)));
                                            }
                                            return Observable.empty();
                                        })));
    }
}
