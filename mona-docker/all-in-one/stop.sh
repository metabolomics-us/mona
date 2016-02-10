#!/bin/bash

# MoNA docker container name
MONA_IMAGE_NAME="mona/all-in-one:latest"

# Stop the docker container
docker stop $(docker ps -a | grep $MONA_IMAGE_NAME | awk '{ print $1 }')