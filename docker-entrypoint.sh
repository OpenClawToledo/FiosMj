#!/bin/sh
set -e
# Copia imagens iniciais para o volume, sem sobrescrever o que já existe
if [ -d /app/seed/uploads ]; then
  cp -rn /app/seed/uploads/. /app/data/uploads/ 2>/dev/null || true
fi
exec java -jar /app/app.jar
