package io.left.rightmesh.libdtn.utils.rxdeserializer;

import java.nio.ByteBuffer;

/**
 * Object Oriented State Pattern. Each state must implement the onNext(Buffer) method to
 * process a newly arrived buffer. Child can optionally implements onEnter and onExit that
 * are called when the system first enters the state and then leave the state.
 *
 * <pre>
 *                        onNext()
 *                           |
 *                           V
 *                    +--------------+
 *  --- onEnter() --> |    STATE      | --- onExit() -->
 *                    +--------------+
 * </pre>
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class RxState {

    public void onEnter() throws RxDeserializerException {
    }

    public abstract void onNext(ByteBuffer next) throws RxDeserializerException;

    public void onExit() throws RxDeserializerException {
    }

    /**
     * debugging.
     */
    private static final boolean DEBUG = false;

    protected void debug(String from, String msg) {
        if (DEBUG) {
            System.out.println(from + "> " + msg);
        }
    }
}
