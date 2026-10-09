#!/data/data/com.termux/files/usr/bin/bash
# Backup do Fios MJ no celular (Termux): banco + fotos, guarda 30 dias.
# Vai para Armazenamento interno/FiosMJ-backups, para dar para mandar ao Google Drive.
# Agendado pelo setup-celular.sh (cron às 03:15).
set -euo pipefail
DATA_DIR="${DATA_DIR:-$HOME/fiosmj-dados}"
DEST="${BACKUP_DIR:-$HOME/storage/shared/FiosMJ-backups}"
KEEP_DAYS="${KEEP_DAYS:-30}"
STAMP=$(date +%Y-%m-%d_%H%M)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$DEST"

# Cópia consistente do banco mesmo com o site no ar
sqlite3 "$DATA_DIR/fiosmj.db" ".backup '$TMP/fiosmj.db'"
tar czf "$DEST/fiosmj-$STAMP.tar.gz" -C "$TMP" fiosmj.db -C "$DATA_DIR" uploads

find "$DEST" -name 'fiosmj-*.tar.gz' -mtime +"$KEEP_DAYS" -delete
echo "[$STAMP] backup ok → $DEST/fiosmj-$STAMP.tar.gz"
