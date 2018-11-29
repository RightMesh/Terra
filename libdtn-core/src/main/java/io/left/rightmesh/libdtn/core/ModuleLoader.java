package io.left.rightmesh.libdtn.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import io.left.rightmesh.libdtn.core.api.CoreAPI;
import io.left.rightmesh.libdtn.core.api.ModuleLoaderAPI;
import io.left.rightmesh.libdtn.core.spi.aa.ApplicationAgentAdapterSPI;
import io.left.rightmesh.libdtn.core.spi.cla.ConvergenceLayerSPI;
import io.left.rightmesh.libdtn.core.spi.core.CoreModuleSPI;

import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_AA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_CLA_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.ENABLE_CORE_MODULES;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_AA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_CLA_PATH;
import static io.left.rightmesh.libdtn.core.api.ConfigurationAPI.CoreEntry.MODULES_CORE_PATH;

/**
 * @author Lucien Loiseau on 25/10/18.
 */
public class ModuleLoader implements ModuleLoaderAPI  {

    private static final String TAG = "ModuleLoader";

    private CoreAPI core;

    ModuleLoader(CoreAPI core) {
        this.core = core;
        loadAAModulesFromDirectory();
        loadCLAModulesFromDirectory();
        loadCoreModulesFromDirectory();
    }

    @Override
    public void loadAAModule(ApplicationAgentAdapterSPI aa) {
        aa.init(core.getRegistrar(),
                core.getConf(),
                core.getLogger(),
                core.getExtensionManager(),
                core.getStorage().getBlobFactory());
        core.getLogger().i(TAG, "AA module loaded: " + aa.getModuleName()+" - UP");
    }

    @Override
    public void loadCLAModule(ConvergenceLayerSPI cla) {
        core.getClaManager().addCLA(cla);
        core.getLogger().i(TAG, "CLA module loaded: " + cla.getModuleName()+" - UP");
    }

    @Override
    public void loadCoreModule(CoreModuleSPI cm) {
        cm.init(core);
        core.getLogger().i(TAG, "CORE module loaded: " + cm.getModuleName()+" - UP");
    }

    private void loadAAModulesFromDirectory() {
        if (core.getConf().<Boolean>get(ENABLE_AA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_AA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<ApplicationAgentAdapterSPI> sl = ServiceLoader.load(ApplicationAgentAdapterSPI.class, ucl);
                for (ApplicationAgentAdapterSPI aa : sl) {
                    if(core.getConf().<Boolean>getModuleEnabled(aa.getModuleName(), false).value()) {
                        loadAAModule(aa);
                    } else {
                        core.getLogger().i(TAG, "AA module loaded: " + aa.getModuleName()+" - DOWN");
                    }
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading AA module: " + e.getMessage());
            }
        }
    }

    private void loadCLAModulesFromDirectory() {
        if (core.getConf().<Boolean>get(ENABLE_CLA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_CLA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<ConvergenceLayerSPI> sl = ServiceLoader.load(ConvergenceLayerSPI.class, ucl);
                for (ConvergenceLayerSPI cla : sl) {
                    if(core.getConf().getModuleEnabled(cla.getModuleName(), false).value()) {
                        loadCLAModule(cla);
                    } else {
                        core.getLogger().i(TAG, "CLA module loaded: " + cla.getModuleName()+" - DOWN");
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
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<CoreModuleSPI> sl = ServiceLoader.load(CoreModuleSPI.class, ucl);
                for (CoreModuleSPI cm : sl) {
                    if(core.getConf().getModuleEnabled(cm.getModuleName(), false).value()) {
                        loadCoreModule(cm);
                    } else {
                        core.getLogger().i(TAG, "CORE module loaded: " + cm.getModuleName()+" - DOWN");
                    }
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading CORE module: " + e.getMessage());
            }
        }
    }

    private URL[] pathToListOfJarURL(String path) throws Exception {
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
