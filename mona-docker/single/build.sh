#!/bin/bash

set -o pipefail


IMAGE="mona/site"
VERSION=`date +%F_%H-%M-%S`

docker build -t ${IMAGE}:${VERSION} . | tee build.log || exit 1
ID=$(tail -1 build.log | awk '{print $3;}')
docker tag -f $ID ${IMAGE}:latest

