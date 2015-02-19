#!/bin/bash
#build an actual image ready for deployment

sh build.sh
docker save mona:latest > mona.tar

