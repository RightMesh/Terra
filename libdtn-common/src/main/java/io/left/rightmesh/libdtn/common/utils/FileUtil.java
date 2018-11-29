package io.left.rightmesh.libdtn.common.utils;

import java.io.File;
import java.io.IOException;

/**
 * @author Lucien Loiseau on 28/10/18.
 */
public class FileUtil {

    static String fileSeparator = System.getProperty("file.separator");

    public static File createNewFile(String prefix, String suffix, String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return File.createTempFile(prefix, suffix, f);
        } else {
            throw new IOException();
        }
    }

    public static File createFile(String filename, String path) throws IOException {
        File p = new File(path);
        if (p.exists() && p.canRead() && p.canWrite()) {
            File f = new File(path + fileSeparator + filename);
            if(f.createNewFile()) {
                return f;
            }
        }
        throw new IOException();
    }

    public static long spaceLeft(String path) {
        File f = new File(path);
        if (f.exists() && f.canRead() && f.canWrite()) {
            return f.getUsableSpace();
        } else {
            return 0;
        }
    }
}
