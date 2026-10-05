#!/usr/bin/env bash
# Instala o Fios MJ num computador de casa com Linux (Ubuntu, Debian, Linux Mint
# ou Raspberry Pi OS 64 bits) e publica na internet pelo Cloudflare Tunnel.
#
# Uso (no computador de casa):
#   curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-casa.sh | sudo bash
#
# Antes: crie o túnel no painel da Cloudflare e copie o token (veja o README).
# Pode rodar de novo sem problema.
set -euo pipefail

REPO="${REPO:-https://github.com/OpenClawToledo/FiosMj.git}"
APP_DIR=/opt/apps/fiosmj
[ "$(id -u)" -eq 0 ] || { echo "❌ Rode com sudo"; exit 1; }
step() { echo; echo "==> $*"; }
ask() { local v; read -r -p "$1" v < /dev/tty || true; echo "$v"; }
setenv() {  # setenv CHAVE valor  (cria ou troca a linha no .env; aceita qualquer caractere)
  grep -v "^$1=" .env > .env.tmp || true
  printf '%s=%s\n' "$1" "$2" >> .env.tmp
  mv .env.tmp .env && chmod 600 .env
}

step "1/7 Docker"
if ! command -v docker >/dev/null || ! docker compose version >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable --now docker
command -v git >/dev/null || { apt-get update -q && apt-get install -yq git curl openssl; }

step "2/7 Baixando o projeto em $APP_DIR"
mkdir -p /opt/apps
if [ -d "$APP_DIR/.git" ]; then git -C "$APP_DIR" pull -q; else git clone -q "$REPO" "$APP_DIR"; fi
cd "$APP_DIR"
chmod +x deploy.sh scripts/*.sh

step "3/7 Configuração (.env)"
[ -f .env ] || cp .env.example .env
chmod 600 .env
setenv COMPOSE_FILE docker-compose.casa.yml   # todos os "docker compose" usam a versão de casa
JWT=$(grep -E '^JWT_SECRET=' .env | cut -d= -f2- || true)
[ ${#JWT} -ge 32 ] || setenv JWT_SECRET "$(openssl rand -hex 32)"

ADMIN=$(grep -E '^ADMIN_SECRET=' .env | cut -d= -f2- || true)
while [ ${#ADMIN} -lt 12 ]; do
  ADMIN=$(ask "   Senha do painel /admin (mínimo 12 caracteres, sem espaço, \$ ou aspas): ")
  case "$ADMIN" in *[[:space:]]*|*'$'*|*'"'*|*"'"*) echo "   ⚠️  Não use espaço, \$ ou aspas."; ADMIN=""; continue;; esac
  [ ${#ADMIN} -ge 12 ] || echo "   ⚠️  Muito curta."
done
setenv ADMIN_SECRET "$ADMIN"

TOKEN=$(grep -E '^TUNNEL_TOKEN=' .env | cut -d= -f2- || true)
while [ ${#TOKEN} -lt 50 ]; do
  TOKEN=$(ask "   Cole o token do Cloudflare Tunnel (começa com eyJ...): ")
done
setenv TUNNEL_TOKEN "$TOKEN"

MP=$(grep -E '^MP_ACCESS_TOKEN=' .env | cut -d= -f2- || true)
if [ -z "$MP" ]; then
  MP=$(ask "   Access Token do Mercado Pago (Enter para preencher depois): ")
  [ -n "$MP" ] && setenv MP_ACCESS_TOKEN "$MP"
  PK=$(ask "   Public Key do Mercado Pago (Enter para preencher depois): ")
  [ -n "$PK" ] && setenv MP_PUBLIC_KEY "$PK"
fi

step "4/7 Computador sem hibernar (o site cai se ele dormir)"
systemctl mask sleep.target suspend.target hibernate.target hybrid-sleep.target >/dev/null 2>&1 || true
if [ -f /etc/systemd/logind.conf ]; then
  sed -i 's/^#\?HandleLidSwitch=.*/HandleLidSwitch=ignore/; s/^#\?HandleLidSwitchExternalPower=.*/HandleLidSwitchExternalPower=ignore/' /etc/systemd/logind.conf
  grep -q '^HandleLidSwitch=' /etc/systemd/logind.conf || echo 'HandleLidSwitch=ignore' >> /etc/systemd/logind.conf
  systemctl restart systemd-logind >/dev/null 2>&1 || true
fi

step "5/7 Construindo e subindo (a primeira vez leva de 5 a 15 minutos)"
docker compose up -d --build

step "6/7 Conferindo"
for i in $(seq 1 60); do
  curl -fsS http://127.0.0.1:8081/api/health >/dev/null 2>&1 && { echo "   ✅ App respondendo neste computador"; break; }
  sleep 5
done
sleep 5
if docker compose logs --tail=40 tunnel 2>/dev/null | grep -qi "Registered tunnel connection"; then
  echo "   ✅ Túnel da Cloudflare conectado"
else
  echo "   ⚠️  Túnel ainda não confirmou. Veja:  sudo docker compose -f $APP_DIR/docker-compose.casa.yml logs tunnel"
fi

step "7/7 Backup diário às 03:15 (guarda 30 dias em /opt/backups/fiosmj)"
( crontab -l 2>/dev/null | grep -v 'fiosmj/scripts/backup.sh' ;
  echo "15 3 * * * $APP_DIR/scripts/backup.sh >> /var/log/fiosmj-backup.log 2>&1" ) | crontab -

cat <<EOF

────────────────────────────────────────────────────────────
Pronto. Se o túnel estiver configurado, o site abre em https://fiosmj.com
Painel: https://fiosmj.com/admin  (senha = a que você digitou)

Atualizar depois:  cd $APP_DIR && sudo ./deploy.sh
Backups ficam NESTE computador: copie /opt/backups/fiosmj para fora
(Google Drive, pendrive) pelo menos 1 vez por semana.
────────────────────────────────────────────────────────────
EOF
