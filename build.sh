#!/bin/bash

#simple build file for our docker image of the latest version

#build client module
cd moa-client

npm install
bower update

grunt dist
cd ..

#build server module

cd moa-server
grails clean
grails war
cd ..

#assmeble docker file

eval "$(docker-machine env cloud)"

cd mona-docker/single

cp ../../moa-server/target/mona-server.war root.war
cp ../../moa-client/mona-client.zip client.zip

bash build.sh push


cd ../cloud

cp ../../moa-server/target/mona-server.war root.war
cp ../../moa-client/mona-client.zip client.zip

$(boot2docker shellinit)

bash build.sh push


cd ../cache

bash build.sh push
