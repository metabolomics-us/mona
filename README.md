# README #

ports for cluster service nodes:
- persistence svr: 2222
- configuration svr: 1111
- discovery service: 8761
- auth server: 3333
- proxy service: 8080 (entry point)


ports for in-memory node:
- embedded mongo db: 27017
- mona rest client: 8080
- elasticsearch client: 9200
- elasticsearch web???: 9300

Building the nodes:

Common:
- check/setup docker configuration
	for Mac/Win:
	  eval "$(docker-machine env default)"
	for linux:
	  N/A
	  
- run 'mvn clean install' on the backend folder to compile and create the docker images

To run the in-memory node docker continer:
- To create the containers manually:
	run 'docker run [--name <whateveryouwanttonameyourimage>] -p=80:8080 [-p=9200:9200 -p=9300:9300 -p=27017:27017] eros.fiehnlab.ucdavis.edu/mona-memory-node' 
		to start the docker container of choice

To run the cluster service nodes using docker compose: (WIP)
- To run the cluster node using docker-compose:
	'cd app/compose'
	run 'docker-compose up -d'

