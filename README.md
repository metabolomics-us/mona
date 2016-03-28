# README #

To build the mongo based nodes:

- check/setup docker configuration
	for mac/win:
	  eval $(docker-machine env default)
	for linux:
	  N/A
- run 'mvn clean install' on the backend folder to compile and create the docker images

- run 'docker run --name <whateveryouwanttonameyourimage> eros.fiehnlab.ucdavis.edu/<memory-node | node>' to start the docker container of choice (in memory or a cluster node)

