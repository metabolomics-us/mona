#!/bin/bash

# Usage: maintenance_mode.sh on|off|status
#   Toggles the nginx maintenance page by touching/removing a flag file that
#   nginx checks live on every request, no reload or restart needed.

# cd into repo root
cd "$(dirname "$0")/.." || exit 1

FLAG_FILE="backend/nginx_v2/maintenance/maintenance.flag"

case "$1" in
    on)
        mkdir -p "$(dirname "$FLAG_FILE")"
        touch "$FLAG_FILE"
        echo "Maintenance mode ON: $FLAG_FILE"
        ;;
    off)
        rm -f "$FLAG_FILE"
        echo "Maintenance mode OFF: $FLAG_FILE removed"
        ;;
    status)
        if [ -f "$FLAG_FILE" ]; then
            echo "Maintenance mode is ON"
        else
            echo "Maintenance mode is OFF"
        fi
        ;;
    *)
        echo "Usage: $0 on|off|status"
        exit 1
        ;;
esac
