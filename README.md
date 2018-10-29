# LibDTN - a lightweight and modular DTN library

Delay Tolerant Networking is a network architecture providing communications in and/or through highly stressed environments. Stressed networking environments include those with intermittent connectivity, large and/or variable delays, and high bit error rates.  Key capabilities of BP include:

* Ability to use physical mobility for the movement of data
* Ability to cope with intermittent connectivity, including cases where the sender and receiver are not concurrently present in the network
* Ability to take advantage of scheduled, predicted, and opportunistic connectivity, whether bidirectional or unidirectional, in addition to continuous connectivity
* Late binding of overlay network endpoint identifiers to underlying constituent network addresses

The protocol used for DTN is called the **Bundle Protocol** (BP) and is soon-to-be an RFC standard from the IETF. A **Bundle** is a protocol data unit of BP, so named because negotiation of the parameters of a data exchange may be impractical in a delay-tolerant network: it is often better practice to "bundle" with a unit of data all metadata that might be needed in order to make the data immediately usable when delivered to applications.  The BP uses **Endpoint Identifier** (EID) to identify the destination or the source of a Bundle. An EID is an URI and so in essence is a string that can be arbitrarily long but must comply to the URI scheme. An EID is not necessarily unique across the entire network but each DTN node must register to at least one Singleton EID. 

The bundle protocol sits between a transport layer and the application layer.

* The Transport layer “transport” data from one node to another, that includes: TCP, UDP, HTTP but also USB (as in a sneakerNet), Mail (SMTP), etc. The interface between the bundle protocol and a specific underlying transport is termed as a "Convergence Layer Adapter" (CLA). A opens channels (CLAChannel) that can be used to send or receive bundles.

* The application layer represents the layer that invoke the services of the BP for any purpose. It pushes down data that are then encapsulated into bundles and may also receive data. Application registers to a specific EID to receive Bundle. It usually is the current node EID + a path (for instance: dtn:this-dtn-node/an-application). That path is also called a “sink”.

# Feature

* libdtn-core is lightweight and bpbis compliant
* modular architecture with plugins that can be loaded during runtime
* libdtn-module-stcp (SimpleTCP) - convergence layer adapter module
* libdtn-module-ldcp (LibDtn Client Protocol) - application agent module to remote client
* libdtn-module-http core module for querying the dtn-node

* binaries:
* Terra - a full dtn node
* dtncat - netcat of dtn, it is light client that can register/recv/send bundles from a full dtn node using LDCP.






