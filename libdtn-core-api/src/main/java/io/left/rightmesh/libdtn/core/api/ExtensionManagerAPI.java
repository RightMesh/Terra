package io.left.rightmesh.libdtn.core.api;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.bundleV7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.eid.CLAEIDParser;
import io.left.rightmesh.libdtn.common.data.eid.EIDSspParser;

/**
 * @author Lucien Loiseau on 22/11/18.
 */
public interface ExtensionManagerAPI extends ExtensionToolbox {

    class BlockTypeAlreadyManaged extends Exception {
    }

    class EIDSchemeAlreadyManaged extends Exception {
    }

    class CLNameAlreadyManaged extends Exception {
    }

    /**
     * Add a new ExtensionBlock.
     *
     * @param type block type
     * @param block block supplier
     * @param parser parser supplier
     * @param serializer serializer supplier
     * @throws BlockTypeAlreadyManaged if the block is already managed
     */
    void addExtensionBlock(int type,
                           Supplier<CanonicalBlock> block,
                           Supplier<CborParser> parser,
                           Supplier<CborEncoder> serializer,
                           Supplier<BlockProcessor> processor) throws BlockTypeAlreadyManaged;

    /**
     * Add a new EID family.
     *
     * @param schemeId the IANA number for this EID scheme
     * @param schemeStr EID scheme
     * @param ssPparser scheme specific parser
     * @throws EIDSchemeAlreadyManaged if the EID is already managed
     */
    void addExtensionEID(int schemeId,
                         String schemeStr,
                         EIDSspParser ssPparser) throws EIDSchemeAlreadyManaged;

    /**
     * Add a new EID BaseCLAEID family.
     *
     * @param cl_name EID scheme
     * @param parser scheme specific parser
     * @throws CLNameAlreadyManaged if the EID is already managed
     */
    void addExtensionCLA(String cl_name,
                         CLAEIDParser parser) throws CLNameAlreadyManaged;
}
