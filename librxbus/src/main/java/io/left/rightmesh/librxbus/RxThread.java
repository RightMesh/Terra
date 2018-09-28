package io.left.rightmesh.librxbus;

/**
 * Thread type that can be used with the {@see Subscribe} annotation.
 *
 * @author Lucien Loiseau on 03/08/18.
 */
public enum RxThread {
    POSTING,
    MAIN,
    IO,
    NEW,
    COMPUTATION,
    TRAMPOLINE
}
