# README #

To build the mongo based nodes:

- check/setup docker configuration
	for Mac/Win:
	  eval "$(docker-machine env default)"
	for linux:
	  N/A
- run 'mvn clean install' on the backend folder to compile and create the docker images

- To create the containers manually:
	run 'docker run [--name <whateveryouwanttonameyourimage>] -p 80:8080 [-p::9200 -p::9300 -p::27017] -eros.fiehnlab.ucdavis.edu/<memory-node | node>' 
		to start the docker container of choice (in memory or a cluster node)
		port 8080: maps the web client to port 80 on docker host (required if you want to use the system)
		port 9200: maps elasticsearch command-line client to same port on host (optional)
		port 9300: maps elasticsearch web client to same port on host (optional)
		port 27017: maps mongo client to same port on host

- To run the cluster node using docker-compose:
	'cd app/compose'
	run 'docker-compose up -d'

