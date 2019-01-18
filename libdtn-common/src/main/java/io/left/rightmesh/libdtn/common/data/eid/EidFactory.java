package io.left.rightmesh.libdtn.common.data.eid;

/**
 * Factory to create Eid from parsing the eid scheme-specific part.
 *
 * @author Lucien Loiseau on 28/11/18.
 */
public interface EidFactory extends EidSspParser {

    class UnknownIanaNumber extends Exception {
        public UnknownIanaNumber(int ianaNumber) {
            super("Unknown ianaNumber number: " + ianaNumber);
        }
    }


    class UnknownEidScheme extends EidFormatException {
        public UnknownEidScheme(String scheme) {
            super("unknown Eid Scheme: " + scheme);
        }
    }

    /**
     * Returns the scheme matching an Eid ianaNumber number.
     *
     * @param ianaScheme id (integer)
     * @return String representing the scheme of the Eid
     * @throws UnknownIanaNumber if ianaScheme is unknown
     */
    String getIanaScheme(int ianaScheme) throws UnknownIanaNumber;

    /**
     * Parse a Eid.
     *
     * @param str entire Eid to parse
     * @return new Eid
     * @throws EidFormatException if there was an error while parsing the Eid
     */
    Eid create(String str) throws EidFormatException;

    /**
     * Parse a Eid.
     *
     * @param scheme Eid scheme
     * @param ssp    Eid scheme specific part
     * @return new Eid
     * @throws EidFormatException if there was an error while parsing the Eid
     */
    Eid create(String scheme, String ssp) throws EidFormatException;
}
