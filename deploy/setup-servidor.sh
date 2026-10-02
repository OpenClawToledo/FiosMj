#!/usr/bin/env bash
# Prepara um servidor Ubuntu 22.04/24.04 NOVO para o Fios MJ.
# Pensado para a Oracle Cloud grátis (ARM Ampere A1), mas serve em qualquer VPS.
#
# Uso (no servidor novo):
#   curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-servidor.sh | sudo bash
#
# Se existir /root/fiosmj.env ou ~ubuntu/fiosmj.env (o .env copiado da KVM), ele é usado.
# O script pode ser rodado de novo sem problema.
set -euo pipefail

REPO="${REPO:-https://github.com/OpenClawToledo/FiosMj.git}"
APP_DIR=/opt/apps/fiosmj
[ "$(id -u)" -eq 0 ] || { echo "❌ Rode com sudo"; exit 1; }
export DEBIAN_FRONTEND=noninteractive

step() { echo; echo "==> $*"; }

step "1/8 Atualizando o sistema"
apt-get update -q
apt-get upgrade -yq

step "2/8 Instalando Docker, Nginx, Certbot e proteção contra invasão (fail2ban)"
echo iptables-persistent iptables-persistent/autosave_v4 boolean true | debconf-set-selections
echo iptables-persistent iptables-persistent/autosave_v6 boolean true | debconf-set-selections
apt-get install -yq docker.io docker-compose-v2 nginx certbot python3-certbot-nginx \
  fail2ban unattended-upgrades git curl openssl iptables-persistent
systemctl enable --now docker nginx fail2ban
dpkg-reconfigure -f noninteractive unattended-upgrades >/dev/null 2>&1 || true

step "3/8 Liberando as portas 80 e 443 (a imagem Ubuntu da Oracle bloqueia tudo menos SSH)"
for p in 80 443; do
  if ! iptables -C INPUT -p tcp --dport "$p" -m state --state NEW -j ACCEPT 2>/dev/null; then
    pos=$(iptables -L INPUT --line-numbers | awk '/REJECT/ {print $1; exit}')
    iptables -I INPUT "${pos:-1}" -p tcp --dport "$p" -m state --state NEW -j ACCEPT
  fi
done
netfilter-persistent save >/dev/null

step "4/8 Memória extra (swap de 2 GB) para o build não travar"
if ! swapon --show | grep -q .; then
  fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile >/dev/null && swapon /swapfile
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

step "5/8 Baixando o projeto em $APP_DIR"
mkdir -p /opt/apps
if [ -d "$APP_DIR/.git" ]; then git -C "$APP_DIR" pull -q; else git clone -q "$REPO" "$APP_DIR"; fi
chmod +x "$APP_DIR"/deploy.sh "$APP_DIR"/scripts/*.sh
docker volume create fiosmj_data >/dev/null

step "6/8 Configuração (.env)"
cd "$APP_DIR"
if [ ! -f .env ]; then
  for f in /root/fiosmj.env /home/ubuntu/fiosmj.env; do
    if [ -f "$f" ]; then cp "$f" .env; echo "   usando $f (copiado da KVM)"; break; fi
  done
fi
if [ ! -f .env ]; then
  cp .env.example .env
  sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$(openssl rand -hex 32)|" .env
  echo "   ⚠️  .env novo criado. Preencha ADMIN_SECRET e MP_* com:  sudo nano $APP_DIR/.env"
fi
chmod 600 .env

step "7/8 Nginx (site fiosmj.com)"
cp deploy/nginx-fiosmj.conf /etc/nginx/sites-available/fiosmj
ln -sf /etc/nginx/sites-available/fiosmj /etc/nginx/sites-enabled/fiosmj
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

step "8/8 Backup diário às 03:15 (guarda 30 dias em /opt/backups/fiosmj)"
( crontab -l 2>/dev/null | grep -v 'fiosmj/scripts/backup.sh' ;
  echo "15 3 * * * $APP_DIR/scripts/backup.sh >> /var/log/fiosmj-backup.log 2>&1" ) | crontab -

ADMIN=$(grep -E '^ADMIN_SECRET=' .env | cut -d= -f2- || true)
JWT=$(grep -E '^JWT_SECRET=' .env | cut -d= -f2- || true)
if [ ${#ADMIN} -ge 12 ] && [ ${#JWT} -ge 32 ]; then
  echo; echo "==> Construindo e subindo o app (no ARM leva de 5 a 15 minutos na primeira vez)"
  docker compose up -d --build
  for i in $(seq 1 60); do
    curl -fsS http://127.0.0.1:8081/api/health >/dev/null 2>&1 && { echo "✅ App no ar em 127.0.0.1:8081"; break; }
    sleep 5
  done
else
  echo; echo "⏸️  App ainda não iniciado: falta ADMIN_SECRET (mín. 12) ou JWT_SECRET (mín. 32) no .env."
  echo "    Depois de preencher:  cd $APP_DIR && sudo docker compose up -d --build"
fi

IP=$(curl -fsS -4 https://ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')
cat <<EOF

────────────────────────────────────────────────────────────
Servidor pronto. IP público: $IP

Próximos passos:
 1. Trazer os dados da KVM:  sudo $APP_DIR/scripts/restore.sh /caminho/fiosmj-AAAA-MM-DD.tar.gz
 2. No painel do domínio, apontar fiosmj.com e www para: $IP
 3. Quando o domínio já abrir neste servidor, ativar o HTTPS:
    sudo certbot --nginx -d fiosmj.com -d www.fiosmj.com --redirect -m SEU_EMAIL --agree-tos -n
────────────────────────────────────────────────────────────
EOF
