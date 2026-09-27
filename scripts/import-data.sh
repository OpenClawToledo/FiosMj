#!/usr/bin/env bash
# Importa o banco e as imagens da instalação antiga (OpenClaw) para o volume Docker.
# Uso: ./scripts/import-data.sh /caminho/para/fiosmj.db [/caminho/para/uploads]
# Rode com o app PARADO:  docker compose stop app
set -euo pipefail
DB="${1:?informe o caminho do fiosmj.db}"
UPLOADS="${2:-}"
[ -f "$DB" ] || { echo "❌ $DB não existe"; exit 1; }

docker run --rm -v fiosmj_data:/data -v "$(realpath "$DB")":/src/fiosmj.db:ro alpine \
  sh -c 'mkdir -p /data/uploads && cp /src/fiosmj.db /data/fiosmj.db'

if [ -n "$UPLOADS" ]; then
  [ -d "$UPLOADS" ] || { echo "❌ $UPLOADS não existe"; exit 1; }
  docker run --rm -v fiosmj_data:/data -v "$(realpath "$UPLOADS")":/src/uploads:ro alpine \
    sh -c 'cp -r /src/uploads/. /data/uploads/'
fi

# O container roda como usuário não-root "fiosmj"
docker compose run -T --rm --no-deps --user root --entrypoint chown app -R fiosmj:fiosmj /app/data
echo "✅ Dados importados. Suba com: docker compose up -d"
