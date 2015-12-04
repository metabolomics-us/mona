#!/bin/bash

#simple build file for our docker image of the latest version

if [ "$1" != "--skip-compile" ]
then
	#build client module
	cd moa-client

	npm install
	bower update
	rm mona-client.zip
	grunt clean
	grunt dist
	cd ..

	#build server module
	cd moa-server
	rm -rdf target/*
	grails clean
	grails war
	cd ..
fi

#assmeble docker file
if [ "$HOSTNAME" == "trashcan.fiehnlab.ucdavis.edu" ]
then
  eval "$(docker-machine env cloud)"
fi

cd mona-docker/single

cp ../../moa-server/target/mona-server.war root.war
cp ../../moa-client/mona-client.zip client.zip

bash build.sh push

if [ "$HOSTNAME" == "trashcan.fiehnlab.ucdavis.edu" ]
then
  cd ../cloud

  cp ../../moa-server/target/mona-server.war root.war
  cp ../../moa-client/mona-client.zip client.zip

  $(boot2docker shellinit)

  bash build.sh push
fi

cd ../cache

bash build.sh push
