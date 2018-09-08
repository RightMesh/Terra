package io.left.rightmesh.libdtn.utils.rxparser;

import java.nio.ByteBuffer;

/**
 * MetaState is an RxState with its own internal RxState.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class MetaState extends RxState {

    private RxState current;
    private boolean done = false;

    @Override
    public void onEnter() throws RxParserException {
        done = true;
        changeState(initState());
    }


    public abstract RxState initState();

    @Override
    public void onNext(ByteBuffer next) throws RxParserException {
        if (current == null) {
            throw new RxParserException("MetaState", "Inner State is not set");
        }
        if (done) {
            throw new RxParserException("MetaState", "Unexpected bytes");
        }
        current.onNext(next);
    }

    public void done() {
        this.done = true;
    }

    protected void changeState(RxState state) {
        this.current = state;
    }
}
