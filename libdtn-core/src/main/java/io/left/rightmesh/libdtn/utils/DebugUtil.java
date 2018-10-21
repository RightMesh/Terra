package io.left.rightmesh.libdtn.utils;

import java.nio.ByteBuffer;
import java.util.Formatter;

/**
 * @author Lucien Loiseau on 20/09/18.
 */
public class DebugUtil {

    public static void printBuffer(String prefix, ByteBuffer buf) {
        if(buf == null) {
            return;
        }
        buf.mark();
        Formatter formatter = new Formatter();
        formatter.format(prefix + " 0x");
        while (buf.hasRemaining()) {
            formatter.format("%02x", buf.get());
        }
        buf.reset();
        System.out.println(formatter.toString());
    }

}
