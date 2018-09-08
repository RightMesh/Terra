package io.left.rightmesh.libdtn.network;

/**
 * The role of the discovery agent is to discover the local peers on all the interface  available
 * and throws appropriate Events whenever there is a change to the topology. It should be able to
 * detect Bluetooth neighbor, WiFi neighbor and neighbor over Internet link (such as superpeers).
 *
 * <p>The Discovery Agent, basically acts as a scanner and maintain a local routing table. Those
 * features are mostly already part of the RightMesh middleware and so it is mostly listening for
 * the RightMeshEvents to maintain the local routing table.
 *
 * @author Lucien Loiseau on 16/07/18.
 */
public class DiscoveryAgent {
}
