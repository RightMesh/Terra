package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.STCPAgent;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RegistrationTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.routing.StaticRouting;
import io.left.rightmesh.libdtn.data.Bundle;
import io.left.rightmesh.libdtn.network.cla.STCP;
import io.left.rightmesh.libdtn.storage.SimpleStorage;
import io.left.rightmesh.libdtn.storage.VolatileStorage;

/**
 * DTNCore registers all the DTN Core Component and is the entry point for all Bundles.
 * Bundles are first emitted upstream either by an application agent (through the API) or by a
 * Convergence Layer. At this point the Bundle follows the following workflow:
 *
 * <pre>
 *                    +-------+  +-------------------+
 *                    |  API  |  | Convergence Layer |
 *                    +-------+  +-------------------+
 *                          ||    ||
 *                        +------------+
 *                  +---< |  UPSTREAM  | <-------+
 *                  |     +------------+         |
 *                  |                            |
 *                  |                  +-----------------+
 *                  |                  | Notify Upstream |
 *                  |                  +-----------------+
 *                  V                          ^   ^
 *          +----------------+                 |   |
 *          | onDeserialized | --- REJECTED ---+   |
 *          +----------------+                 |   |
 *                  |                          |   |
 *                  V                          |   |
 *             +--------+                      |   |
 *             | INJECT |                      |   |
 *             +--------+                      |   |
 *                  |                          |   |
 *                  V                          |   |
 *            +--------------+                 |   |
 *            | onProcessing | ---- REJECTED --+   |
 *            +--------------+                     |
 *                  |                              |
 *                  +------ CUSTODY ACCEPTED ------+
 *                  |
 *                  |
 *                  +<-----------------------------------------------+
 *                  |                                                |
 *                  V                                                |
 *            +------------+                                         |
 *            |  ROUTING   | >-- NO ROUTE FOUND ----+                |
 *            +------------+          |             |                |
 *                  |                 |             |                |
 *                  V                 |             V                |
 *      +--------------------------+  |    +----------------+        |
 *      | onPrepareForTransmission |  |    | onPutOnStorage |        |
 *      +--------------------------+  |    +----------------+        |
 *                  |                 |             |                |
 *                  V                 ^             V                |
 *            +------------+          |       +-----------+          |
 *            |  TRANSMIT  |          |       |  STORAGE  |          |
 *            +------------+          |       +-----------+          |
 *                  |                 |             |                |
 *                  V                 |             V                |
 *           +---------------+        |    +-------------------+     |
 *           | Peer Accepted | >- NO -+    | onPullFromStorage | >---+
 *           +---------------+             +-------------------+
 *                  |
 *                  V
 *             +---------+
 *             | RELEASE |
 *             +---------+
 *
 * </pre>
 *
 * <p>For each of the following steps, the CoreProcessor will process the Bundle for validity-check
 * or to update block specific data if needed. For instance some bundles may be rejected as soon
 * as the deserialization step (rejected endpoint, lifetime expired), some bundles may need to
 * be fully received to be decrypted and check at the processing step, some blocks may require
 * to be updated just before transmission (like the AgeBlock), etc. Some of those decisions are
 * the reponsability of the Core depending to the DTNConfiguration, but most of it are
 * block-specific, for this reason extension blocks are required to provide implementation for \
 * the callback.
 *
 * <table>
 *   <tr>
 *     <td> onDeserialized </td> <td> process individual deserialized bundle component </td>
 *   </tr>
 *   <tr>
 *     <td> onProcessing </td> <td> process a whole new bundle </td>
 *   </tr>
 *   <tr>
 *     <td> onPrepareForTransmission </td> <td> process a bundle for transmission </td>
 *   </tr>
 *   <tr>
 *     <td> onPutOnStorage </td> <td> process a bundle for parking into storage </td>
 *   </tr>
 *   <tr>
 *     <td> onPullFromStorage </td> <td> process a bundle as it just got pulled from storage </td>
 *   </tr>
 * </table>
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore {

    // ---- SINGLETON ----
    private static DTNCore instance = new DTNCore();
    public static DTNCore getInstance() {   return instance;   }
    
    private DTNCore() {
        // init all the components and load configuration
        LocalEIDTable.getInstance();
        VolatileStorage.getInstance();
        SimpleStorage.getInstance();
        LinkLocalRouting.getInstance();
        StaticRouting.getInstance();
        SmartRouting.getInstance();
        RegistrationTable.getInstance();
        STCPAgent.getInstance();
    }

    /**
     * Inject a bundle in the system. It may be a bundle received from a convergence layer
     * or a bundle received from an application agent. At this point the Bundle is assumed
     * to have been validated already.
     *
     * @param bundle to inject
     */
    public static void inject(Bundle bundle) {
    }
}
