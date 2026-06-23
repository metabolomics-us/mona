#!/bin/bash

if [ "$1" = "stop" ]; then
    echo "============================="
    echo "  STOPPING DOCKER SERVICES   "
    echo "============================="
    # Stop full suite of microservices
    docker-compose -f backend/docker-compose-dev.yml down
    exit 0
fi

echo "============================="
echo "  STARTING DOCKER SERVICES   "
echo "============================="
# Start full suite of microservices
docker-compose -f backend/docker-compose-dev.yml up -d
