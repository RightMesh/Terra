package io.left.rightmesh.libdtn.core.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Lucien Loiseau on 22/10/18.
 */
public class ReflectionUtil {

    public static <T>  List<T> loadClass(String directory, String classpath, Class<T> parentClass) throws ClassNotFoundException {
        File pluginsDir = new File(System.getProperty("user.dir") + directory);
        if(!pluginsDir.isDirectory()) {
            throw new ClassNotFoundException("not a directory: "+System.getProperty("user.dir") + directory);
        }
        if(pluginsDir.listFiles() == null) {
            throw new ClassNotFoundException("jar not found in path: "+System.getProperty("user.dir") + directory);
        }
        List<T> classes = new ArrayList<>();
        for (File jar : pluginsDir.listFiles()) {
            try {
                ClassLoader loader = URLClassLoader.newInstance(
                        new URL[] { jar.toURI().toURL() },
                        Thread.currentThread().getClass().getClassLoader()
                );
                Class<?> clazz = Class.forName(classpath, true, loader);
                Class<? extends T> newClass = clazz.asSubclass(parentClass);
                // Apparently its bad to use Class.newInstance, so we use
                // newClass.getConstructor() instead
                Constructor<? extends T> constructor = newClass.getConstructor();
                classes.add(constructor.newInstance());

            } catch (ClassNotFoundException e) {
                continue;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    public static List<Class<?>> findClassesImplementing(final Class<?> interfaceClass, final Package fromPackage) {
        if (interfaceClass == null) {
            return null;
        }
        if (fromPackage == null) {
            return null;
        }
        final List<Class<?>> rVal = new ArrayList<Class<?>>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(fromPackage.getName());
            if (targets != null) {
                for (Class<?> aTarget : targets) {
                    if (aTarget == null) {
                        continue;
                    }
                    else if (aTarget.equals(interfaceClass)) {
                        continue;
                    }
                    else if (!interfaceClass.isAssignableFrom(aTarget)) {
                        continue;
                    }
                    else if( Modifier.isAbstract( aTarget.getModifiers() )) {
                        continue;
                    }
                    else {
                        rVal.add(aTarget);
                    }
                }
            }
        }
        catch (ClassNotFoundException | IOException e) {
            /* ignore */
        }
        return rVal;
    }

    public static Class[] getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}