#!/bin/bash

# cd into repo root
cd "$(dirname "$0")/.." || exit 1

# Parse args order-independently: one may be the "stop" action, the other the environment
STOP=false
ENV="dev"
for arg in "$1" "$2"; do
    case "$arg" in
        stop)
            STOP=true
            ;;
        dev|test|local)
            ENV="$arg"
            ;;
    esac
done

if [ "$STOP" == true ]; then
    echo "============================"
    echo "  STOPPING DOCKER SERVICES   "
    echo "============================"
    echo -e "Environment arg passed: $ENV\n"
    docker-compose -f backend/docker-compose-"$ENV".yml down
    exit 0
fi

echo "============================"
echo "  STARTING DOCKER SERVICES   "
echo "============================"
echo -e "Environment arg passed: $ENV\n"
docker-compose -f backend/docker-compose-"$ENV".yml up -d
