#!/bin/bash

# Name of the MoNA docker image
IMAGE_NAME="mona/all-in-one"

# Build docker image
docker build -t $IMAGE_NAME --rm=true . | tee build.log || exit 1

# Tag the docker container
ID=$(tail -1 build.log | awk '{print $3;}')
docker tag -f $ID $IMAGE_NAME:latest