#!/usr/bin/env bash
# Atualiza o Fios MJ na KVM: ./deploy.sh
set -euo pipefail
cd "$(dirname "$0")"

[ -f .env ] || { echo "❌ Falta o .env (cp .env.example .env)"; exit 1; }

echo "📥 git pull"
git pull --ff-only

echo "💾 backup antes de atualizar"
./scripts/backup.sh || echo "⚠️  backup falhou, continuando"

echo "🔨 build + restart"
docker compose up -d --build

echo "⏳ aguardando a aplicação..."
for i in $(seq 1 30); do
  if curl -fsS http://127.0.0.1:8081/api/health >/dev/null 2>&1; then
    echo "✅ Fios MJ no ar"
    docker image prune -f >/dev/null
    exit 0
  fi
  sleep 3
done

echo "❌ A aplicação não respondeu. Últimos logs:"
docker compose logs --tail=50 app
exit 1
