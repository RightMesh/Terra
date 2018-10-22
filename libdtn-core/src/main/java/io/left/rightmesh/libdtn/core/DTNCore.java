package io.left.rightmesh.libdtn.core;

import io.left.rightmesh.libdtn.core.agents.http.APIDaemonHTTPAgent;
import io.left.rightmesh.libdtn.core.agents.APIStaticApplicationAgent;
import io.left.rightmesh.libdtn.core.network.ConnectionAgent;
import io.left.rightmesh.libdtn.core.processor.BundleProcessor;
import io.left.rightmesh.libdtn.core.routing.AARegistrar;
import io.left.rightmesh.libdtn.core.routing.LinkLocalRouting;
import io.left.rightmesh.libdtn.core.routing.LocalEIDTable;
import io.left.rightmesh.libdtn.core.routing.RoutingEngine;
import io.left.rightmesh.libdtn.core.routing.RoutingTable;
import io.left.rightmesh.libdtn.core.routing.SmartRouting;
import io.left.rightmesh.libdtn.core.network.DiscoveryAgent;
import io.left.rightmesh.libdtn.core.network.CLAManager;
import io.left.rightmesh.libdtn.core.storage.bundle.Storage;
import io.left.rightmesh.libdtn.core.utils.Log;

/**
 * DTNCore registers all the DTN Core BaseComponent.
 *
 * @author Lucien Loiseau on 24/08/18.
 */
public class DTNCore {

    public static final String TAG = "DTNCore";

    private DTNConfiguration conf;
    private Log log;
    private LocalEIDTable localEIDTable;
    private LinkLocalRouting linkLocalRouting;
    private RoutingTable routingTable;
    private SmartRouting smartRouting;
    private RoutingEngine routingEngine;
    private AARegistrar aaRegistrar;
    private Storage storage;
    private BundleProcessor bundleProcessor;
    private ConnectionAgent connectionAgent;
    private APIStaticApplicationAgent staticApi;
    private APIDaemonHTTPAgent httpApi;
    private DiscoveryAgent discoveryAgent;
    private CLAManager claManager;

    public static DTNCore init(DTNConfiguration conf) {
        DTNCore core = new DTNCore();
        core.conf = conf;
        core.log = new Log(conf);
        core.localEIDTable = new LocalEIDTable(core);
        core.linkLocalRouting = new LinkLocalRouting(core);
        core.routingTable = new RoutingTable(core);
        core.routingEngine = new RoutingEngine(core);
        core.smartRouting = new SmartRouting(core);
        core.aaRegistrar = new AARegistrar(core);
        core.storage = new Storage(core.conf, core.log);
        core.bundleProcessor = new BundleProcessor(core);
        core.connectionAgent = new ConnectionAgent(core);
        core.discoveryAgent = new DiscoveryAgent(core);
        core.staticApi = new APIStaticApplicationAgent(core);
        core.httpApi = new APIDaemonHTTPAgent(core);
        core.discoveryAgent = new DiscoveryAgent(core);
        core.claManager = new CLAManager(core);
        return core;
    }

    public DTNConfiguration getConf() {
        return conf;
    }

    public Log getLogger() {
        return log;
    }

    public LocalEIDTable getLocalEIDTable() {
        return localEIDTable;
    }

    public LinkLocalRouting getLinkLocalRouting() {
        return linkLocalRouting;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public RoutingEngine getRoutingEngine() {
        return routingEngine;
    }

    public AARegistrar getRegistrar() {
        return aaRegistrar;
    }

    public Storage getStorage() {
        return storage;
    }

    public APIStaticApplicationAgent staticAPI() {
        return staticApi;
    }

    public ConnectionAgent getConnectionAgent() {
        return connectionAgent;
    }

    public DiscoveryAgent getDiscoveryAgent() {
        return discoveryAgent;
    }

    public BundleProcessor getBundleProcessor() {
        return bundleProcessor;
    }

}
