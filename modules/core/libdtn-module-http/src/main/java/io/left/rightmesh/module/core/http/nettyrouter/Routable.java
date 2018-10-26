package io.left.rightmesh.module.core.http.nettyrouter;

/**
 * A Routable can inject routing information to a router at runtime,
 * allowing route composition, data driven routes, and routing plugin.
 */
@FunctionalInterface
public interface Routable<I, O> {
    /**
     * Update the given router with new routing information.
     *
     * @param router the router to update
     */
    void registerWith(Router<I, O> router);
}