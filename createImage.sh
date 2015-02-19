#!/bin/bash
#build an actual image ready for deployment

sh build.sh

$(boot2docker shellinit)

docker save mona:latest > mona.tar

