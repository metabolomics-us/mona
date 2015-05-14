#!/bin/bash

NAME="mona/cache"
IMAGE="gose.fiehnlab.ucdavis.edu:5000/$NAME"
VERSION=`date +%F_%H-%M-%S`

docker build -t ${IMAGE} --rm=true . | tee build.log || exit 1
ID=$(tail -1 build.log | awk '{print $3;}')
docker tag -f $ID ${IMAGE}:latest
docker tag -f $ID ${NAME}:latest

echo "push: $1"
if [ "$1" == "push" ]; then
  echo "pushing $IMAGE to server"
  docker push $IMAGE
else
  echo "pushing disabled - use 'push' as argument to upload to the gose server!"
fi
