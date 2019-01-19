package io.left.rightmesh.libdtn.core;

import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.ENABLE_AA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.ENABLE_CLA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.ENABLE_CORE_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.MODULES_AA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.MODULES_CLA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationApi.CoreEntry.MODULES_CORE_PATH;

import io.left.rightmesh.libdtn.core.api.CoreApi;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderApi;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSpi;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSpi;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSpi;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * ModuleLoader implements the ModuleLoaderApi. It provides static method to load module as well
 * as the ability to load module from a directory given as part of the configuration.
 *
 * @author Lucien Loiseau on 25/10/18.
 */
public class ModuleLoader extends CoreComponent implements ModuleLoaderApi {

    private static final String TAG = "ModuleLoader";

    private CoreApi core;

    /**
     * Constructor.
     *
     * @param core reference to the core
     */
    public ModuleLoader(CoreApi core) {
        this.core = core;
    }

    @Override
    public String getComponentName() {
        return TAG;
    }

    @Override
    protected void componentUp() {
        loadAaModulesFromDirectory();
        loadClaModulesFromDirectory();
        loadCoreModulesFromDirectory();
    }

    @Override
    protected void componentDown() {
        // todo unload all modules
    }

    @Override
    public void loadAaModule(ApplicationAgentAdapterSpi aa) {
        if (!isEnabled()) {
            return;
        }

        aa.init(core.getRegistrar(),
                core.getConf(),
                core.getLogger(),
                core.getExtensionManager(),
                core.getStorage().getBlobFactory());
        core.getLogger().i(TAG, "AA module loaded: " + aa.getModuleName() + " - UP");
    }

    @Override
    public void loadClaModule(ConvergenceLayerSpi cla) {
        if (!isEnabled()) {
            return;
        }

        core.getClaManager().addCla(cla);
        core.getLogger().i(TAG, "CLA module loaded: " + cla.getModuleName() + " - UP");
    }

    @Override
    public void loadCoreModule(CoreModuleSpi cm) {
        if (!isEnabled()) {
            return;
        }

        cm.init(core);
        core.getLogger().i(TAG, "CORE module loaded: " + cm.getModuleName() + " - UP");
    }

    private void loadAaModulesFromDirectory() {
        if (core.getConf().<Boolean>get(ENABLE_AA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_AA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarUrl(path));
                ServiceLoader<ApplicationAgentAdapterSpi> sl
                        = ServiceLoader.load(ApplicationAgentAdapterSpi.class, ucl);
                for (ApplicationAgentAdapterSpi aa : sl) {
                    if (core.getConf().<Boolean>getModuleEnabled(aa.getModuleName(), false)
                            .value()) {
                        loadAaModule(aa);
                    } else {
                        core.getLogger().i(TAG, "AA module loaded: " + aa.getModuleName()
                                + " - DOWN");
                    }
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading AA module: " + e.getMessage());
            }
        }
    }

    private void loadClaModulesFromDirectory() {
        if (core.getConf().<Boolean>get(ENABLE_CLA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_CLA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarUrl(path));
                ServiceLoader<ConvergenceLayerSpi> sl
                        = ServiceLoader.load(ConvergenceLayerSpi.class, ucl);
                for (ConvergenceLayerSpi cla : sl) {
                    if (core.getConf().getModuleEnabled(cla.getModuleName(), false)
                            .value()) {
                        loadClaModule(cla);
                    } else {
                        core.getLogger().i(TAG, "CLA module loaded: " + cla.getModuleName()
                                + " - DOWN");
                    }
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading CLA module: " + e.getMessage());
            }
        }
    }

    private void loadCoreModulesFromDirectory() {
        if (core.getConf().<Boolean>get(ENABLE_CORE_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_CORE_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarUrl(path));
                ServiceLoader<CoreModuleSpi> sl = ServiceLoader.load(CoreModuleSpi.class, ucl);
                for (CoreModuleSpi cm : sl) {
                    if (core.getConf().getModuleEnabled(cm.getModuleName(), false)
                            .value()) {
                        loadCoreModule(cm);
                    } else {
                        core.getLogger().i(TAG, "CORE module loaded: " + cm.getModuleName()
                                + " - DOWN");
                    }
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading CORE module: " + e.getMessage());
            }
        }
    }

    private URL[] pathToListOfJarUrl(String path) throws Exception {
        File loc = new File(path);
        File[] flist = loc.listFiles(f -> f.getPath().toLowerCase().endsWith(".jar"));
        if (flist == null) {
            throw new Exception();
        }
        URL[] urls = new URL[flist.length];
        for (int i = 0; i < flist.length; i++) {
            urls[i] = flist[i].toURI().toURL();
        }
        return urls;
    }
}
