#!/bin/bash

# cd into repo root
cd "$(dirname "$0")/.." || exit 1

if [ "$1" == "stop" ]; then
    echo "============================"
    echo "  STOPPING DOCKER SERVICES   "
    echo "============================"
    if [ "$2" == "dev" ] || [ "$2" == "test" ] || [ "$2" == "local" ]; then
        echo -e "Environment arg passed: $2\n"
        docker-compose -f backend/docker-compose-"$2".yml down
        exit 0
    fi
    # default to dev
    docker-compose -f backend/docker-compose-dev.yml down
    exit 0
fi

echo "============================"
echo "  STARTING DOCKER SERVICES   "
echo "============================"
if [ "$1" == "dev" ] || [ "$1" == "test" ] || [ "$1" == "local" ]; then
    echo -e "Environment arg passed: $1\n"
    docker-compose -f backend/docker-compose-"$1".yml up -d
    exit 0
fi

# default to dev
docker-compose -f backend/docker-compose-dev.yml up -d
