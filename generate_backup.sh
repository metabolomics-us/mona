#!/bin/bash
# NOTE: This script is meant to be run from the Gose server only (or wherever MoNA is hosted in the future)

# To initiate a restore: docker exec -i backend-postgresql-1 pg_restore -U <user> -d mona --data-only --disable-triggers < /home/jvogel/mona_backups/mona-YYYY-MM-DD.dump

set -euo pipefail

TIMESTAMP=$(date +"%Y-%m-%d")
BACKUP_DIR="/home/jvogel/mona_backups"
BACKUP_FILE="${BACKUP_DIR}/mona-${TIMESTAMP}.dump"
BACKUP_TEMP="${BACKUP_FILE}.tmp"
MAX_BACKUPS=3
CONTAINER="backend-postgresql-1"

mkdir -p "$BACKUP_DIR"

trap 'rm -f "$BACKUP_TEMP"' EXIT
trap 'echo "[$(date)] ERROR: backup failed" >&2' ERR

echo "[$(date)] Starting backup of database 'mona' to ${BACKUP_FILE}"
docker exec "$CONTAINER" bash -c 'pg_dump -Fc --data-only -U "${POSTGRES_USER:?POSTGRES_USER not set}" -d "mona"' > "$BACKUP_TEMP"

if [ ! -s "$BACKUP_TEMP" ]; then
    echo "[$(date)] ERROR: Backup file is empty. Aborting." >&2
    exit 1
fi

mv "$BACKUP_TEMP" "$BACKUP_FILE"
echo "[$(date)] Backup complete: $(du -sh "$BACKUP_FILE" | cut -f1)"

# Remove oldest backups beyond MAX_BACKUPS to save space
pruned=$(ls -1t "${BACKUP_DIR}"/mona-*.dump 2>/dev/null | tail -n +"$((MAX_BACKUPS + 1))")
if [[ -n "$pruned" ]]; then
    echo "$pruned" | xargs rm -f
    echo "[$(date)] Pruned old backups, keeping ${MAX_BACKUPS} most recent"
fi
