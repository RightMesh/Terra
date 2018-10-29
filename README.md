# LibDTN - a lightweight and modular DTN library

Delay Tolerant Networking is a network architecture providing communications in and/or through highly stressed environments. Stressed networking environments include those with intermittent connectivity, large and/or variable delays, and high bit error rates.  Key capabilities of BP include:

* Ability to use physical mobility for the movement of data
* Ability to cope with intermittent connectivity, including cases where the sender and receiver are not concurrently present in the network
* Ability to take advantage of scheduled, predicted, and opportunistic connectivity, whether bidirectional or unidirectional, in addition to continuous connectivity
* Late binding of overlay network endpoint identifiers to underlying constituent network addresses

This library is still a work in progress - no stable version yet.

# Description

Libraries:
* libdtn-core is a modular and lightweight implementation of [bpbis-11](https://tools.ietf.org/html/draft-ietf-dtn-bpbis-11)

Modules:
* libdtn-module-stcp (SimpleTCP) - convergence layer adapter module, implementation of [draft-stcp-00](https://www.ietf.org/internet-drafts/draft-burleigh-dtn-stcp-00.txt)
* libdtn-module-ldcp (LibDtn Client Protocol) - application agent module to remote client
* libdtn-module-http core module for querying the dtn-node

binaries:
* terra - a full dtn node application daemon
* dtncat - netcat of dtn, it is light client that can register/recv/send bundles from a remote dtn node using LDCP.
* dtnping - work in progress

## License

    Copyright 2018 RightMesh

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.






