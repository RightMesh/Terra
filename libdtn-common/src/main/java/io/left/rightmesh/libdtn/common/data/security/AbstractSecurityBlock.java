package io.left.rightmesh.libdtn.common.data.security;

import io.left.rightmesh.libdtn.common.data.ExtensionBlock;
import io.left.rightmesh.libdtn.common.data.eid.Eid;

import java.util.LinkedList;

/**
 * AbstractSecurityBlock is an ExtensionBlock for the bpsec extension.
 *
 * @author Lucien Loiseau on 03/11/18.
 */
public abstract class AbstractSecurityBlock extends ExtensionBlock implements SecurityBlock {

    public LinkedList<Integer> securityTargets;
    public int cipherSuiteId;
    public int securityBlockFlag;
    public Eid securitySource;
    public LinkedList<LinkedList<SecurityResult>> securityResults;


    AbstractSecurityBlock(int type) {
        super(type);
        securityTargets = new LinkedList<>();
        securityResults = new LinkedList<>();
    }

    AbstractSecurityBlock(AbstractSecurityBlock block) {
        super(block.type);
        cipherSuiteId = block.cipherSuiteId;
        securityBlockFlag = block.securityBlockFlag;
        securitySource = block.securitySource;
        securityTargets = new LinkedList<>();
        securityTargets.addAll(block.securityTargets);
        securityResults = new LinkedList<>();
        for (LinkedList<SecurityResult> lsr : block.securityResults) {
            LinkedList lsrcopy = new LinkedList<>();
            lsrcopy.addAll(lsr);
            securityResults.add(lsrcopy);
        }
    }

    @Override
    public Eid getSecuritySource() {
        return securitySource;
    }

    @Override
    public boolean getSaFlag(SecurityBlockFlags flag) {
        return ((securityBlockFlag >> flag.ordinal()) & 0x1) == 0x1;
    }

    @Override
    public int getCipherSuiteId() {
        return cipherSuiteId;
    }

    public void setSecuritySource(Eid source) {
        this.securitySource = source;
        setSaFlag(SecurityBlockFlags.SECURITY_SOURCE_PRESENT, true);
    }

    /**
     * add a target block for this SecurityBlock.
     *
     * @param number of the targeted block.
     */
    public void addTarget(int number) {
        if (!securityTargets.contains(number)) {
            securityTargets.add(number);
        }
    }

    /**
     * Set/clear a {@link SecurityBlockFlags} on this SecurityBlock.
     *
     * @param flag  the flag to be set/clear
     * @param state true to set, false to clear
     */
    void setSaFlag(SecurityBlockFlags flag, boolean state) {
        if (state) {
            securityBlockFlag |= (1 << flag.ordinal());
        } else {
            securityBlockFlag &= ~(1 << flag.ordinal());
        }
    }

}
