#!/bin/bash

export NVM_DIR=$HOME/.nvm;
source $NVM_DIR/nvm.sh;
nvm use 14.16.1
echo "============================="
echo "     STARTING FRONTEND       "
echo "============================="
#Start Frontend Dev
cd backend/app/server/proxy
ng serve


