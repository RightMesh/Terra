package io.left.rightmesh.libdtn.common.utils;

import java.io.File;
import java.io.IOException;

/**
 * FileUtil provides helper method to manipulate file and directory.
 *
 * @author Lucien Loiseau on 28/10/18.
 */
public class FileUtil {

    static String fileSeparator = System.getProperty("file.separator");

    /**
     * creates a new file.
     *
     * @param prefix file prefix.
     * @param suffix file suffix.
     * @param path file directory.
     * @return a new File.
     * @throws IOException if the file could not be created.
     */
    public static File createNewFile(String prefix, String suffix, String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return File.createTempFile(prefix, suffix, f);
        } else {
            throw new IOException();
        }
    }

    /**
     * create a new file.
     *
     * @param filename name of the file.
     * @param path directory of the file.
     * @return a new file.
     * @throws IOException if the file could not be created.
     */
    public static File createFile(String filename, String path) throws IOException {
        File p = new File(path);
        if (p.exists() && p.canRead() && p.canWrite()) {
            File f = new File(path + fileSeparator + filename);
            if (f.createNewFile()) {
                return f;
            }
        }
        throw new IOException();
    }

    /**
     * check how many space is left on the given directory.
     *
     * @param path directory to check for space left.
     * @return space left in bytes.
     */
    public static long spaceLeft(String path) {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return f.getUsableSpace();
        } else {
            return 0;
        }
    }
}
