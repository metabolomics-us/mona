#!/bin/bash

#simple build file for our docker image of the latest version

#build client module
cd moa-client
grunt dist
cd ..

#build server module

cd moa-server
grails war
cd ..

#assmeble docker file

cd mona-docker/single

cp ../../moa-server/target/mona-server.war root.war
cp ../../moa-client/mona-client.zip client.zip

$(boot2docker shellinit)

docker build -t mona .
