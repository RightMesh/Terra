package io.left.rightmesh.libdtn.common.data.security;


import io.left.rightmesh.libdtn.common.data.BlockFactory;
import io.left.rightmesh.libdtn.common.data.Bundle;
import io.left.rightmesh.libdtn.common.data.bundleV7.parser.BlockDataParserFactory;
import io.left.rightmesh.libdtn.common.data.bundleV7.serializer.BlockDataSerializerFactory;
import io.left.rightmesh.libdtn.common.data.eid.EID;
import io.left.rightmesh.libdtn.common.utils.Log;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public interface SecurityBlock {


    class NoSuchBlockException extends Exception {
        NoSuchBlockException() {
        }
        NoSuchBlockException(String msg) {
            super(msg);
        }
    }

    class ForbiddenOperationException extends Exception {
        ForbiddenOperationException() {
        }
        ForbiddenOperationException(String msg) {
            super(msg);
        }
    }

    class SecurityOperationException extends Exception {
        SecurityOperationException() {
        }
        SecurityOperationException(String msg) {
            super(msg);
        }
    }


    enum SecurityBlockFlags {
        RESERVED_1,
        RESERVED_2,
        RESERVED_3,
        SECURITY_SOURCE_PRESENT,
        RESERVED_5,
    }

    /**
     * return the cipher suite Id for this security block
     * @return cipher suite id
     */
    int getCipherSuiteId();

    /**
     * add a security source to this SecurityBlock.
     *
     * @param source
     */
    void setSecuritySource(EID source);

    /**
     * get the security source to this SecurityBlock.
     *
     * @return EID security source
     */
    EID getSecuritySource();

    /**
     * Get the state of a specific {@link SecurityBlockFlags} from this SecurityBlock.
     *
     * @param flag to query
     * @return true if the flag is set, false otherwise
     */
    boolean getSAFlag(SecurityBlockFlags flag);

    /**
     * add a block number as a target.
     *
     * @param number
     */
    void addTarget(int number);

    /**
     * try to add this SecurityBlock to a given Bundle. This checks for block interaction.
     *
     * @param bundle to apply this SecurityBlock to.
     */
    void addTo(Bundle bundle) throws ForbiddenOperationException, NoSuchBlockException;
}
