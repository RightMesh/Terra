package io.left.rightmesh.libdtn.bundleV6;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.HashMap;

/**
 * Dictionary implements a dictionary of EID to be used during the serialization of Bundle.
 *
 * @author Lucien Loiseau on 23/07/18.
 */
public class Dictionary {

    class EntryNotFoundException extends Throwable {
        EntryNotFoundException(String msg) {
            super(msg);
        }
    }

    private HashMap<String, Integer> map;
    private ByteArrayDataOutput dict;
    private int offset;

    /**
     * Create an empty dictionary.
     */
    Dictionary() {
        map = new HashMap<>();
        dict = ByteStreams.newDataOutput();
        offset = 0;
    }

    /**
     * Create a dictionary with all EID from a given Bundle.
     *
     * @param bundle to be used for the dictionary
     */
    Dictionary(Bundle bundle) {
        map = new HashMap<>();
        dict = ByteStreams.newDataOutput();
        offset = 0;
        add(bundle);
    }

    /**
     * Create a dictionary from another dictionary.
     *
     * @param dict to be cloned
     */
    Dictionary(Dictionary dict) {
        map = dict.map;
        this.dict = dict.dict;
        offset = dict.offset;
    }

    /**
     * Create a dictionary from a byte array.
     *
     * @param dict byte array dictionary
     */
    Dictionary(byte[] dict) {
        map = new HashMap<>();
        this.dict = ByteStreams.newDataOutput();
        offset = 0;

        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < dict.length; pos++) {
            if (dict[pos] == 0x00) {
                add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append((char) dict[pos]);
            }
        }
    }

    /**
     * Clear the dictionary.
     */
    public void clear() {
        this.map.clear();
        this.dict = ByteStreams.newDataOutput();
        this.offset = 0;
    }

    /**
     * add all EIDs from a Bundle to the dictionary.
     *
     * @param bundle to be used to fill the dictionary
     */
    public void add(Bundle bundle) {
        add(bundle.source);
        add(bundle.destination);
        add(bundle.reportto);
        add(bundle.custodian);

        // we add all the EIDs present in block headers if any
        for (Block block : bundle.getBlocks()) {
            for (EID eid : block.getEids()) {
                add(eid);
            }
        }
    }

    /**
     * add an EID to the dictionary.
     *
     * @param eid to add to the dictionary
     */
    public void add(EID eid) {
        add(eid.getScheme());
        add(eid.getSsp());
    }

    /**
     * Add a String to the dictionary.
     *
     * @param str to add to the dictionary
     */
    private void add(String str) {
        if (map.containsKey(str)) {
            return;
        }
        byte[] bytes = str.getBytes();
        map.put(str, offset);
        dict.write(bytes);
        dict.write(0x00);
        offset += bytes.length + 1;
    }

    /**
     * Given a string given as a parameter, returns the offset position in the current dictionary.
     *
     * @param str a string
     * @return offset position in dictionary
     * @throws EntryNotFoundException if string isn't found in dictionary
     */
    public int getOffset(String str) throws EntryNotFoundException {
        if (!map.containsKey(str)) {
            throw new EntryNotFoundException(str);
        }
        return map.get(str);
    }

    /**
     * Lookup a string in the Dictionary using its reference.
     *
     * @param offset position of the string
     * @return String at the offset
     * @throws EntryNotFoundException if the offset is not within bound of the dictionary
     */
    public String lookup(int offset) throws EntryNotFoundException {
        if (offset >= this.offset) {
            throw new EntryNotFoundException("offset not within bound: offset=" + offset
                    + " limit=" + this.offset);
        }
        byte[] dictionary = getBytes();
        StringBuilder sb = new StringBuilder();
        while ((dictionary[offset] != 0x00) && (offset < dictionary.length)) {
            sb.append((char) dictionary[offset++]);
        }
        return sb.toString();
    }

    /**
     * Return the Dictionary as an array of bytes formed by concatenating the null-terminated
     * scheme names and SSPs together.
     *
     * @return byte array containing the dictionary
     */
    public byte[] getBytes() {
        return dict.toByteArray();
    }

}
