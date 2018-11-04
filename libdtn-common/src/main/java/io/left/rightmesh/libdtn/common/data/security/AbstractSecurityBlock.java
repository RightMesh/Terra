package io.left.rightmesh.libdtn.common.data.security;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import io.left.rightmesh.libdtn.common.data.ExtensionBlock;
import io.left.rightmesh.libdtn.common.data.eid.EID;

/**
 * @author Lucien Loiseau on 03/11/18.
 */
public abstract class AbstractSecurityBlock extends ExtensionBlock implements SecurityBlock {

    AbstractSecurityBlock(int type) {
        super(type);
    }

    LinkedList<Integer> securityTargets;
    int cipherSuiteId;
    int securityBlockFlag;
    EID securitySource;
    List<List<SecurityResult>> securityResults;

    AbstractSecurityBlock(AbstractSecurityBlock block) {
        super(block.type);
        cipherSuiteId = block.cipherSuiteId;
        securityBlockFlag = block.securityBlockFlag;
        securitySource = block.securitySource;
        securityTargets = new LinkedList<>();
        securityTargets.addAll(block.securityTargets);
    }

    @Override
    public EID getSecuritySource() {
        return securitySource;
    }

    @Override
    public boolean getSAFlag(SecurityBlockFlags flag) {
        return ((securityBlockFlag >> flag.ordinal()) & 0x1) == 0x1;
    }

    @Override
    public int getCipherSuiteId() {
        return cipherSuiteId;
    }

    /**
     * add a security source to this SecurityBlock.
     *
     * @param source
     */
    public void setSecuritySource(EID source) {
        this.securitySource = source;
        setSAFlag(SecurityBlockFlags.SECURITY_SOURCE_PRESENT, true);
    }


    /**
     * Set/clear a {@link SecurityBlockFlags} on this SecurityBlock.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    void setSAFlag(SecurityBlockFlags flag, boolean state) {
        if (state) {
            securityBlockFlag |= (1 << flag.ordinal());
        } else {
            securityBlockFlag &= ~(1 << flag.ordinal());
        }
    }

}
