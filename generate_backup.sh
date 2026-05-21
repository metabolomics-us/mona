#!/bin/bash
# NOTE: This script is meant to be run from the Gose server only (or wherever MoNA is hosted in the future)

# To initiate a restore: docker exec -i backend-postgresql-1 psql -U mona -d mona < /home/jvogel/mona_backups/dump-MM-DD-YYYY.sql

set -euo pipefail

TIMESTAMP=$(date +"%m-%d-%Y")
BACKUP_DIR="/home/jvogel/mona_backups"
BACKUP_FILE="${BACKUP_DIR}/dump-${TIMESTAMP}.sql"
MAX_BACKUPS=3
CONTAINER="backend-postgresql-1"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Starting backup of database 'mona' to ${BACKUP_FILE}"
docker exec "$CONTAINER" bash -c 'pg_dump --disable-triggers --data-only -U "$POSTGRES_USER" -d "mona"' > "$BACKUP_FILE"
echo "[$(date)] Backup complete: $(du -sh "$BACKUP_FILE" | cut -f1)"

# Remove oldest backups beyond MAX_BACKUPS
BACKUP_COUNT=$(ls -1 "${BACKUP_DIR}"/dump-*.sql 2>/dev/null | wc -l)
if [ "$BACKUP_COUNT" -gt "$MAX_BACKUPS" ]; then
    ls -1t "${BACKUP_DIR}"/dump-*.sql | tail -n +"$((MAX_BACKUPS + 1))" | xargs rm -f
    echo "[$(date)] Pruned old backups, keeping ${MAX_BACKUPS} most recent"
fi
