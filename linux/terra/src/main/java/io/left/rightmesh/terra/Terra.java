package io.left.rightmesh.terra;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import io.left.rightmesh.libdtn.common.data.eid.BaseEIDFactory;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.data.eid.EIDFormatException;
import io.left.rightmesh.libdtn.common.utils.Log;
import io.left.rightmesh.libdtn.core.DTNConfiguration;
import io.left.rightmesh.libdtn.core.DTNCore;
import io.left.rightmesh.libdtn.core.api.CoreAPI;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_SIMPLE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.COMPONENT_ENABLE_VOLATILE_STORAGE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_BUNDLE;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_CLA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_CORE_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_FORWARDING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_STATUS_REPORTING;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.LOCAL_EID;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.LOG_LEVEL;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_AA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_CLA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_CORE_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.SIMPLE_STORAGE_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.VOLATILE_BLOB_STORAGE_MAX_CAPACITY;
import static io.left.rightmesh.terra.Terra.StorageOption.BOTH;
import static io.left.rightmesh.terra.Terra.StorageOption.NONE;
import static io.left.rightmesh.terra.Terra.StorageOption.SIMPLE;
import static io.left.rightmesh.terra.Terra.StorageOption.VOLATILE;

@Command(
        name = "terra", mixinStandardHelpOptions = true, version = "terra 1.0",
        header = {
                "@|green          *                                                   .             |@",
                "@|green       +. | .+         .                  .              .         .        |@",
                "@|green   .*.... O   .. *        .                     .       .             .     |@",
                "@|green    ...+.. `'O ..                                    .           |          |@",
                "@|green    +.....+  | ..+                    .                  .      -*-         |@",
                "@|green    ...+...  O ..      ---========================---       .    |          |@",
                "@|green    *...  O'` ...*             .          .                   .             |@",
                "@|green  .    O'`  .+.    _________ ____   _____    _____ .  ___                   |@",
                "@|green        `'O       /___  ___// __/  / __  |  / __  |  /   |                  |@",
                "@|green    .                / /.  / /_   / /_/ /  / /_/ /  / /| | .                |@",
                "@|green      .             / /   / __/  / _  |   / _  |   / __  |          .       |@",
                "@|green               .   /./   / /__  / / | |  / / | |  / /  | |                  |@",
                "@|green    |             /_/   /____/ /_/  |_| /_/  |_| /_/   |_|      .           |@",
                "@|green   -*-                                                                *     |@",
                "@|green    |     .           ---========================---             .          |@",
                "@|green       .                 Terrestrial DTN - v1.0     .                    .  |@",
                "@|green           .    .             *                    .             .          |@",
                "@|green                                  .                         .               |@",
                "@|green ____ /\\__________/\\____ ______________/\\/\\___/\\____________________________|@",
                "@|green                 __                                               ---       |@",
                "@|green          --           -            --  -      -         ---  __            |@",
                "@|green    --  __                      ___--     RightMesh (c) 2018        --  __  |@",
                ""},
        //descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "",
                "Terra is a full node DTN implementation for Terrestrial DTN",},
        optionListHeading = "@|bold %nOptions|@:%n",
        footer = {
                ""})
public class Terra implements Callable<Void> {

    enum StorageOption {NONE, VOLATILE, SIMPLE, BOTH}

    @Option(names = {"-e", "--eid"}, description = "set local eid")
    private String localEID = null;

    @Option(names = {"-s", "--storage"}, description = "storage values: ${COMPLETION-CANDIDATES}")
    private StorageOption storage = NONE;

    @Option(names = {"-P", "--simple-path"}, description = "simple storage directory")
    private String simplePath = "./";

    @Option(names = {"-L", "--volatile-limit"}, description = "volatile storage size limit")
    private int volatileLimit = 1000000;

    @Option(names = {"-d", "--daemon"}, description = "Start Terra as a daemon.")
    private boolean daemon;

    @Option(names = {"--module-cla"}, description = "set the path to the network Convergence Layer Adapters modules.")
    private String claModuleDirectory = null;

    @Option(names = {"--module-aa"}, description = "set the path to the Application Agent Adapters modules.")
    private String aaModuleDirectory = null;

    @Option(names = {"--module-core"}, description = "set the path to the Core modules.")
    private String coreModuleDirectory = null;

    @Option(names = {"-v", "--verbose"}, description = "set the log level to debug (-v -vv -vvv).")
    private boolean[] verbose = new boolean[0];

    @Option(names = {"--disable-reporting"}, description = "disable sending status reporting.")
    private boolean disableReporting = false;

    @Option(names = {"--disable-forwarding"}, description = "do not forward bundle that are not local.")
    private boolean disableForwarding = false;

    @Option(names = {"--disable-eid-autoconnect"}, description = "do not try to create opportunity when dispatching bundles.")
    private boolean disableEidAutoconnect = false;

    @Option(names = {"--disable-peer-autoconnect"}, description = "do not try to create opportunity with detected peers.")
    private boolean disablePeerAutoconnect = false;

    @Option(names = {"--ldcp-port"}, description = "do not try to create opportunity with detected peers.")
    private int ldcpPort = 4557;

    @Option(names = {"--stcp-port"}, description = "do not try to create opportunity with detected peers.")
    private int stcpPort = 4556;

    @Option(names = {"--http-port"}, description = "do not try to create opportunity with detected peers.")
    private int httpPort = 8080;

    @Override
    public Void call() throws Exception {
        DTNConfiguration conf = new DTNConfiguration();

        /* Terra configuration */
        if(localEID != null) {
            try {
                EID eid = new BaseEIDFactory().create(localEID);
                conf.get(LOCAL_EID).update(eid);
            } catch(EIDFormatException efe) {
                throw new Exception("localEID is not a valid Endpoint ID: "+efe.getMessage());
            }
        }

        conf.get(COMPONENT_ENABLE_VOLATILE_STORAGE).update(storage.equals(VOLATILE) || storage.equals(BOTH));
        if (storage.equals(VOLATILE) || storage.equals(BOTH)) {
            conf.get(VOLATILE_BLOB_STORAGE_MAX_CAPACITY).update(volatileLimit);
        }

        conf.get(COMPONENT_ENABLE_SIMPLE_STORAGE).update(storage.equals(SIMPLE) || storage.equals(BOTH));
        if (storage.equals(SIMPLE) || storage.equals(BOTH)) {
            Set<String> paths = new HashSet<>();
            paths.add(simplePath);
            conf.<Set<String>>get(SIMPLE_STORAGE_PATH).update(paths);
        }

        conf.get(ENABLE_STATUS_REPORTING).update(!disableReporting);
        conf.get(ENABLE_FORWARDING).update(!disableForwarding);
        conf.get(ENABLE_AUTO_CONNECT_FOR_BUNDLE).update(!disableEidAutoconnect);
        conf.get(ENABLE_AUTO_CONNECT_FOR_DETECT_EVENT).update(!disablePeerAutoconnect);

        if(claModuleDirectory != null) {
            conf.get(ENABLE_CLA_MODULES).update(true);
            conf.get(MODULES_CLA_PATH).update(claModuleDirectory);
        } else {
            conf.get(ENABLE_CLA_MODULES).update(false);
        }
        if(aaModuleDirectory != null) {
            conf.get(ENABLE_AA_MODULES).update(true);
            conf.get(MODULES_AA_PATH).update(aaModuleDirectory);
        } else {
            conf.get(ENABLE_AA_MODULES).update(false);
        }
        if(coreModuleDirectory != null) {
            conf.get(ENABLE_CORE_MODULES).update(true);
            conf.get(MODULES_CORE_PATH).update(coreModuleDirectory);
        } else {
            conf.get(ENABLE_CORE_MODULES).update(false);
        }

        switch(verbose.length) {
            case 0:
                conf.get(LOG_LEVEL).update(Log.LOGLevel.WARN);
                break;
            case 1:
                conf.get(LOG_LEVEL).update(Log.LOGLevel.INFO);
                break;
            case 2:
                conf.get(LOG_LEVEL).update(Log.LOGLevel.DEBUG);
                break;
            default:
                conf.get(LOG_LEVEL).update(Log.LOGLevel.VERBOSE);
        }

        /* module configuration */
        conf.getModuleEnabled("stcp", true).update(true);
        conf.getModuleEnabled("ldcp", true).update(true);
        conf.getModuleEnabled("hello", true).update(true);
        conf.getModuleEnabled("ipdiscovery", true).update(true);
        conf.getModuleEnabled("http", false).update(false);

        conf.getModuleConf("ldcp","ldcp_tcp_port", 4557).update(ldcpPort);
        conf.getModuleConf("stcp","cla_stcp_port", 4556).update(stcpPort);
        conf.getModuleConf("http","module_http_port", 8080).update(httpPort);

        CoreAPI core = new DTNCore(conf);
        return null;
    }

    public static void main(String[] args) throws Exception {
        CommandLine.call(new Terra(), args);
    }
}
