package io.left.rightmesh.libcbor.rxparser;

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
 *                           |
 *                           V
 *                     Return Next State
 *
 * </pre>
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class ParserState {

    // optional callback
    public void onEnter() throws RxParserException {
    }

    // parser callback
    public abstract ParserState onNext(ByteBuffer next) throws RxParserException;

    // optional callback
    public void onExit() throws RxParserException {
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
