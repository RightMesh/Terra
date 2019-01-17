#!/bin/bash

# collect all jar and binaries to deply
rm -rf ./linux-dtn
mkdir linux-dtn
mkdir linux-dtn/bin
mkdir linux-dtn/lib
mkdir linux-dtn/modules
mkdir linux-dtn/modules/aa
mkdir linux-dtn/modules/cla
mkdir linux-dtn/modules/core
mkdir linux-dtn/storage
tar xvf linux/terra/build/distributions/terra.tar -C linux-dtn/
mv linux-dtn/terra/bin/* linux-dtn/bin
mv linux-dtn/terra/lib/* linux-dtn/lib
rm -rf linux-dtn/terra/
tar xvf linux/dtnping/build/distributions/dtnping.tar -C linux-dtn/
mv linux-dtn/dtnping/bin/* linux-dtn/bin
mv linux-dtn/dtnping/lib/* linux-dtn/lib
rm -rf linux-dtn/dtnping/
tar xvf linux/dtncat/build/distributions/dtncat.tar -C linux-dtn/
mv linux-dtn/dtncat/bin/* linux-dtn/bin
mv linux-dtn/dtncat/lib/* linux-dtn/lib
rm -rf linux-dtn/dtncat/
cp modules/aa/ldcp/libdtn-module-ldcp/build/libs/libdtn-module-ldcp.jar linux-dtn/modules/aa
cp modules/cla/libdtn-module-stcp/build/libs/libdtn-module-stcp.jar linux-dtn/modules/cla
cp modules/core/libdtn-module-hello/build/libs/libdtn-module-hello.jar linux-dtn/modules/core
cp modules/core/libdtn-module-ipdiscovery/build/libs/libdtn-module-ipdiscovery.jar linux-dtn/modules/core

# tar
tar cvzf ./linux-dtn.tar.gz ./linux-dtn/
rm -rf ./linux-dtn

