#!/usr/bin/env bash
# Restaura um backup (gerado por scripts/backup.sh) no volume do Fios MJ.
# Serve para mudar de servidor ou voltar um backup.
# Uso:  sudo ./scripts/restore.sh /caminho/fiosmj-AAAA-MM-DD_HHMM.tar.gz
set -euo pipefail
FILE="${1:?informe o arquivo .tar.gz do backup}"
[ -f "$FILE" ] || { echo "❌ $FILE não existe"; exit 1; }
cd "$(dirname "$0")/.."

echo "Este processo SUBSTITUI o banco e as fotos atuais deste servidor pelo backup."
read -r -p "Continuar? (s/N) " ok < /dev/tty || ok=""
[ "$ok" = "s" ] || [ "$ok" = "S" ] || { echo "Cancelado."; exit 0; }

docker compose stop app 2>/dev/null || true
docker volume create fiosmj_data >/dev/null

# Guarda o que existia antes, por segurança
mkdir -p /opt/backups/fiosmj
docker run --rm -v fiosmj_data:/data:ro -v /opt/backups/fiosmj:/out alpine \
  sh -c '[ -z "$(ls -A /data)" ] || tar czf /out/antes-do-restore-$(date +%Y%m%d%H%M).tar.gz -C /data .'

docker run --rm -v fiosmj_data:/data -v "$(realpath "$FILE")":/backup.tar.gz:ro alpine \
  sh -c 'rm -rf /data/* && tar xzf /backup.tar.gz -C /data'

# O app roda como o usuário "fiosmj" dentro do container
docker compose run -T --rm --no-deps --user root --entrypoint chown app -R fiosmj:fiosmj /app/data
docker compose up -d
echo "✅ Backup restaurado e app reiniciado."
