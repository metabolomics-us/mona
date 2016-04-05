#!/bin/bash


# Directory in which to store the PostgreSQL data, RabbitMQ data and MoNA exports
DATA_DIR=/data/mona

# URL of the local server WITHOUT http:// prefix (if empty, defaults to: localhost)
SITE_URL="gose.fiehnlab.ucdavis.edu"

# Port on which to expose the MoNA web resource
PORT=8080



# MoNA docker container name
MONA_IMAGE_NAME="mona/all-in-one:latest"

# Start up the docker container
docker run -d \
           -h mona \
           -p $PORT:80 \
           -v /etc/localtime:/etc/localtime:ro \
           -v $DATA_DIR:/data \
           -e SITE_URL=$SITE_URL \
           -e SITE_PORT=$PORT \
           $MONA_IMAGE_NAME