#!/bin/bash

echo "============================="
echo "  STARTING DOCKER SERVICES   "
echo "============================="
#Start full suite of microservices
docker-compose -f backend/docker-compose-dev.yml up -d
