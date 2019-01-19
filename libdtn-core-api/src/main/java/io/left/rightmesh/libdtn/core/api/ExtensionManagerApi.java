package io.left.rightmesh.libdtn.core.api;

import io.left.rightmesh.libcbor.CborEncoder;
import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libdtn.common.ExtensionToolbox;
import io.left.rightmesh.libdtn.common.data.CanonicalBlock;
import io.left.rightmesh.libdtn.common.data.bundlev7.processor.BlockProcessor;
import io.left.rightmesh.libdtn.common.data.eid.ClaEidParser;
import io.left.rightmesh.libdtn.common.data.eid.EidSspParser;

import java.util.function.Supplier;

/**
 * API for the extension manager. It enables adding a new extension Block or a new Eid.
 *
 * @author Lucien Loiseau on 22/11/18.
 */
public interface ExtensionManagerApi extends ExtensionToolbox {

    class BlockTypeAlreadyManaged extends Exception {
    }

    class EidSchemeAlreadyManaged extends Exception {
    }

    class ClaNameAlreadyManaged extends Exception {
    }

    /**
     * Add a new ExtensionBlock.
     *
     * @param type block PAYLOAD_BLOCK_TYPE
     * @param block block supplier
     * @param parser parser supplier
     * @param serializer serializer supplier
     * @param processor block processor supplier
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
     * @throws EidSchemeAlreadyManaged if the Eid is already managed
     */
    void addExtensionEid(int schemeId,
                         String schemeStr,
                         EidSspParser ssPparser) throws EidSchemeAlreadyManaged;

    /**
     * Add a new ClaEid family.
     *
     * @param clName Eid scheme
     * @param parser scheme specific parser
     * @throws ClaNameAlreadyManaged if the Eid is already managed
     */
    void addExtensionCla(String clName,
                         ClaEidParser parser) throws ClaNameAlreadyManaged;
}
