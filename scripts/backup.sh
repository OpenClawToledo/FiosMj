#!/usr/bin/env bash
# Backup do banco e das imagens do Fios MJ (fica NA KVM, fora do git).
# Cron diário:  15 3 * * * /opt/apps/fiosmj/scripts/backup.sh >> /var/log/fiosmj-backup.log 2>&1
set -euo pipefail
DEST="${BACKUP_DIR:-/opt/backups/fiosmj}"
KEEP_DAYS="${KEEP_DAYS:-30}"
STAMP=$(date +%Y-%m-%d_%H%M)
mkdir -p "$DEST"

# Compacta o volume inteiro (banco SQLite + imagens)
docker run --rm -v fiosmj_data:/data:ro -v "$DEST":/out alpine \
  tar czf "/out/fiosmj-$STAMP.tar.gz" -C /data .

find "$DEST" -name 'fiosmj-*.tar.gz' -mtime +"$KEEP_DAYS" -delete
echo "[$STAMP] backup ok → $DEST/fiosmj-$STAMP.tar.gz"
