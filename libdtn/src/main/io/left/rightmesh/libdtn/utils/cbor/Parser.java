package io.left.rightmesh.libdtn.utils.cbor;

import io.left.rightmesh.libdtn.utils.rxparser.ParserEmitter;
import io.left.rightmesh.libdtn.utils.rxparser.RxState;
import io.reactivex.Observer;


/**
 * @author Lucien Loiseau on 08/09/18.
 */
public class Parser extends ParserEmitter<Item> {

    Parser(Observer<? super Item> downstream) {
        super(downstream);
    }

    @Override
    public RxState initState() {
        return null;
    }

    @Override
    public void onReset() {
    }
}
