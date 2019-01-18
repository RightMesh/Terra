package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.BaseClaEid;
import io.left.rightmesh.libdtn.common.data.eid.Eid;
import io.reactivex.Observable;

/**
 * @author Lucien Loiseau on 27/11/18.
 */
public interface RoutingTableAPI extends CoreComponentAPI {

    class TableEntry {
        public Eid to;
        public Eid next;

        public TableEntry(Eid to, Eid next) {
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

        public Eid getTo() {
            return to;
        }

        public Eid getNext() {
            return next;
        }

        @Override
        public int hashCode() {
            return next.getEidString().concat(to.getEidString()).hashCode();
        }
    }

    /**
     * Add a route to this routing table.
     *
     * @param to Eid of destination
     * @param nextHop Eid of Next-Hop
     */
    void addRoute(Eid to, Eid nextHop);

    /**
     * Resolve an Eid using this Routing Table.
     *
     * @param destination Eid of destination
     * @return Observable of BaseClaEid-Eid that can make forward progress toward destination
     */
    Observable<BaseClaEid> resolveEID(Eid destination);

    /**
     * Dump all entries from the Routing Table.
     * @return Set of entries.
     */
    Set<TableEntry> dumpTable();

}
