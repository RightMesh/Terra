package io.left.rightmesh.libdtn.core.api;

import java.util.function.Supplier;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;

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
     * @param type block PAYLOAD_BLOCK_TYPE
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
     * Add a new Eid family.
     *
     * @param schemeId the ianaNumber number for this Eid scheme
     * @param schemeStr Eid scheme
     * @param ssPparser scheme specific parser
     * @throws EIDSchemeAlreadyManaged if the Eid is already managed
     */
    void addExtensionEID(int schemeId,
                         String schemeStr,
                         EidSspParser ssPparser) throws EIDSchemeAlreadyManaged;

    /**
     * Add a new Eid BaseClaEid family.
     *
     * @param cl_name Eid scheme
     * @param parser scheme specific parser
     * @throws CLNameAlreadyManaged if the Eid is already managed
     */
    void addExtensionCLA(String cl_name,
                         ClaEidParser parser) throws CLNameAlreadyManaged;
}
