#!/bin/bash

#Setup proxy to connect frontend to microservices backend
echo "============================="
echo "       STARTING CORS         "
echo "============================="
. ~/.nvm/nvm.sh
nvm use 8
CORSPROXY_HOST=0.0.0.0 corsproxy
