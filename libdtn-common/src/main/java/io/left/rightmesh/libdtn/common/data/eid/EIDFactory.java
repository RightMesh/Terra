package io.left.rightmesh.libdtn.common.data.eid;

/**
 * @author Lucien Loiseau on 28/11/18.
 */
public interface EIDFactory extends EIDSspParser {

    class UnknownIanaNumber extends Exception {
        public UnknownIanaNumber(int iana_number) {
            super("Unknown IANA number: "+iana_number);
        }
    }


    class UnknownEIDScheme extends EIDFormatException {
        public UnknownEIDScheme(String scheme) {
            super("unknown EID Scheme: "+scheme);
        }
    }

    /**
     * Returns the scheme matching an EID IANA number
     *
     * @param iana_scheme id (integer)
     * @return String representing the scheme of the EID
     * @throws UnknownIanaNumber if iana_scheme is unknown
     */
    String getIANAScheme(int iana_scheme) throws UnknownIanaNumber;

    /**
     * Parse a EID.
     *
     * @param str entire EID to parse
     * @return new EID
     * @throws EIDFormatException if there was an error while parsing the EID
     */
    EID create(String str) throws EIDFormatException;

    /**
     * Parse a EID.
     *
     * @param scheme EID scheme
     * @param ssp EID scheme specific part
     * @return new EID
     * @throws EIDFormatException if there was an error while parsing the EID
     */
    EID create(String scheme, String ssp) throws EIDFormatException;
}
