package io.left.rightmesh.libdtn.core.api;

import java.util.Set;

import io.left.rightmesh.libdtn.common.data.eid.CLA;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.reactivex.Observable;

/**
 * @author Lucien Loiseau on 27/11/18.
 */
public interface RoutingTableAPI {

    class TableEntry {
        public EID to;
        public EID next;

        public TableEntry(EID to, EID next) {
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

        public EID getTo() {
            return to;
        }

        public EID getNext() {
            return next;
        }

        @Override
        public int hashCode() {
            return next.getEIDString().concat(to.getEIDString()).hashCode();
        }
    }

    /**
     * Add a route to this routing table.
     *
     * @param to EID of destination
     * @param nextHop EID of Next-Hop
     */
    void addRoute(EID to, EID nextHop);


    /**
     * Resolve an EID using this Routing Table.
     *
     * @param destination EID of destination
     * @return Observable of CLA-EID that can make forward progress toward destination
     */
    Observable<CLA> resolveEID(EID destination);

    /**
     * Dump all entries from the Routing Table.
     * @return Set of entries.
     */
    Set<TableEntry> dumpTable();

}
