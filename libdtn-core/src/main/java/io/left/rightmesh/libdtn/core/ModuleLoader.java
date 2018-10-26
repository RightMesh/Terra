package io.left.rightmesh.libdtn.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

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
public class ModuleLoader {

    private static final String TAG = "CLAManager";

    private DTNCore core;


    ModuleLoader(DTNCore core) {
        this.core = core;
        loadAAModules();
        loadCLAModules();
        loadCoreModules();
    }

    private void loadAAModules() {
        if (core.getConf().<Boolean>get(ENABLE_AA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_AA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<ApplicationAgentAdapterSPI> sl = ServiceLoader.load(ApplicationAgentAdapterSPI.class, ucl);
                for (ApplicationAgentAdapterSPI aa : sl) {
                    core.getLogger().i(TAG, "AA module added: " + aa.getModuleName());
                    aa.setLogger(core.getLogger());
                    aa.init(core.getRegistrar());
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading AA module: " + e.getMessage());
            }
        }
    }

    private void loadCLAModules() {
        if (core.getConf().<Boolean>get(ENABLE_CLA_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_CLA_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<ConvergenceLayerSPI> sl = ServiceLoader.load(ConvergenceLayerSPI.class, ucl);
                for (ConvergenceLayerSPI cla : sl) {
                    core.getLogger().i(TAG, "CLA module added: " + cla.getModuleName());
                    cla.setLogger(core.getLogger());
                    core.getClaManager().addCLA(cla);
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading CLA module: " + e.getMessage());
            }
        }
    }

    private void loadCoreModules() {
        if (core.getConf().<Boolean>get(ENABLE_CORE_MODULES).value()) {
            String path = core.getConf().<String>get(MODULES_CORE_PATH).value();
            try {
                URLClassLoader ucl = new URLClassLoader(pathToListOfJarURL(path));
                ServiceLoader<CoreModuleSPI> sl = ServiceLoader.load(CoreModuleSPI.class, ucl);
                for (CoreModuleSPI cm : sl) {
                    core.getLogger().i(TAG, "Core module added: " + cm.getModuleName());
                    cm.init(core);
                }
            } catch (Exception e) {
                core.getLogger().w(TAG, "error loading Core module: " + e.getMessage());
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
