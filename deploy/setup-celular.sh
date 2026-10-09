#!/data/data/com.termux/files/usr/bin/bash
# Instala o Fios MJ num celular Android (testado para Xiaomi/HyperOS) dentro do
# Termux, sem Docker, e publica na internet pelo Cloudflare Tunnel.
#
# Uso (no Termux, instalado pelo F-Droid, NÃO pela Play Store):
#   curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-celular.sh | bash
#
# Antes: crie o túnel no painel da Cloudflare (veja o README, seção do celular).
# Rodar de novo = atualizar: baixa a versão nova, recompila e reinicia.
set -euo pipefail

REPO="${REPO:-https://github.com/OpenClawToledo/FiosMj.git}"
APP_DIR="$HOME/fiosmj"           # código
DATA_DIR="$HOME/fiosmj-dados"    # banco + fotos (nunca apagar)
JAR="$HOME/fiosmj-app.jar"
SV="$PREFIX/var/service"
[ -n "${PREFIX:-}" ] && [ -d "$PREFIX" ] || { echo "❌ Rode dentro do Termux"; exit 1; }
step() { echo; echo "==> $*"; }
ask() { local v; read -r -p "$1" v < /dev/tty || true; echo "$v"; }
setenv() {  # setenv CHAVE valor  (cria ou troca a linha no .env)
  grep -v "^$1=" .env > .env.tmp || true
  printf '%s=%s\n' "$1" "$2" >> .env.tmp
  mv .env.tmp .env && chmod 600 .env
}

step "1/8 Pacotes (Java 17, Node, Maven, cloudflared)"
# "< /dev/null": no "curl | bash", nada pode ler o resto do script como se fosse resposta
export DEBIAN_FRONTEND=noninteractive
pkg upgrade -y -o Dpkg::Options::=--force-confnew < /dev/null
pkg install -y git curl openjdk-17 maven nodejs-lts cloudflared sqlite termux-services cronie < /dev/null

step "2/8 Baixando o projeto em $APP_DIR"
if [ -d "$APP_DIR/.git" ]; then git -C "$APP_DIR" pull -q; else git clone -q "$REPO" "$APP_DIR"; fi
cd "$APP_DIR"
chmod +x deploy/*.sh scripts/*.sh

step "3/8 Configuração (.env)"
[ -f .env ] || cp .env.example .env
chmod 600 .env
JWT=$(grep -E '^JWT_SECRET=' .env | cut -d= -f2- || true)
[ ${#JWT} -ge 32 ] || setenv JWT_SECRET "$(head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n')"

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

step "4/8 Compilando (a primeira vez leva de 5 a 15 minutos)"
( cd frontend && npm ci --no-audit --no-fund && npm run build ) < /dev/null
mvn -q -B package -DskipTests < /dev/null
cp target/fiosmj-app-*.jar "$JAR"
mkdir -p "$DATA_DIR/uploads"
cp -rn seed/uploads/. "$DATA_DIR/uploads/" 2>/dev/null || true   # fotos iniciais, sem sobrescrever

step "5/8 Serviços (reiniciam sozinhos se caírem)"
# Lê o .env sem interpretar nada (senhas e tokens podem ter qualquer caractere)
LOAD_ENV='while IFS= read -r l; do case "$l" in ""|"#"*) continue;; esac; export "$l"; done < '"$APP_DIR"'/.env'
mkdir -p "$SV/fiosmj/log" "$SV/fiosmj-tunnel/log"
cat > "$SV/fiosmj/run" <<EOF
#!$PREFIX/bin/sh
exec 2>&1
$LOAD_ENV
export DB_PATH="$DATA_DIR/fiosmj.db" UPLOADS_PATH="$DATA_DIR/uploads"
cd "$DATA_DIR"
exec java -Xmx512m -XX:+UseSerialGC -jar "$JAR"
EOF
cat > "$SV/fiosmj-tunnel/run" <<EOF
#!$PREFIX/bin/sh
exec 2>&1
$LOAD_ENV
exec cloudflared tunnel --no-autoupdate run --token "\$TUNNEL_TOKEN"
EOF
chmod +x "$SV/fiosmj/run" "$SV/fiosmj-tunnel/run"
for s in fiosmj fiosmj-tunnel; do ln -sf "$PREFIX/share/termux-services/svlogger" "$SV/$s/log/run"; done

# O gerenciador de serviços só sobe sozinho a partir do próximo Termux aberto
. "$PREFIX/etc/profile.d/start-services.sh" 2>/dev/null || true
sleep 3
sv-enable fiosmj; sv-enable fiosmj-tunnel; sv-enable crond
sv restart fiosmj >/dev/null 2>&1 || true
sv restart fiosmj-tunnel >/dev/null 2>&1 || true

step "6/8 Ligar sozinho quando o celular reiniciar (app Termux:Boot)"
mkdir -p "$HOME/.termux/boot"
cat > "$HOME/.termux/boot/fiosmj" <<EOF
#!$PREFIX/bin/sh
termux-wake-lock
. $PREFIX/etc/profile.d/start-services.sh
EOF
chmod +x "$HOME/.termux/boot/fiosmj"
termux-wake-lock || true

step "7/8 Conferindo"
for i in $(seq 1 60); do
  curl -fsS http://127.0.0.1:8081/api/health >/dev/null 2>&1 && { echo "   ✅ App respondendo neste celular"; break; }
  sleep 5
done
curl -fsS http://127.0.0.1:8081/api/health >/dev/null 2>&1 || echo "   ⚠️  App não respondeu. Veja:  tail -50 $PREFIX/var/log/sv/fiosmj/current"
sleep 5
if grep -qi "Registered tunnel connection" "$PREFIX/var/log/sv/fiosmj-tunnel/current" 2>/dev/null; then
  echo "   ✅ Túnel da Cloudflare conectado"
else
  echo "   ⚠️  Túnel ainda não confirmou. Veja:  tail -50 $PREFIX/var/log/sv/fiosmj-tunnel/current"
fi

step "8/8 Backup diário às 03:15 (pasta FiosMJ-backups na memória do celular)"
[ -d "$HOME/storage/shared" ] || { echo "   Permita o acesso aos arquivos na janela que vai abrir."; termux-setup-storage; sleep 5; }
( crontab -l 2>/dev/null | grep -v 'backup-celular.sh' ;
  echo "15 3 * * * $APP_DIR/deploy/backup-celular.sh >> $HOME/fiosmj-backup.log 2>&1" ) | crontab -
"$APP_DIR/deploy/backup-celular.sh" || echo "   ⚠️  Primeiro backup falhou (veja a mensagem acima)."

cat <<EOF

────────────────────────────────────────────────────────────
Pronto. Se o túnel estiver configurado, o site abre em https://fiosmj.com
Painel: https://fiosmj.com/admin  (senha = a que você digitou)

Atualizar depois:  bash $APP_DIR/deploy/setup-celular.sh
Ver o que está acontecendo:  tail -f $PREFIX/var/log/sv/fiosmj/current

IMPORTANTE no Xiaomi (senão o Android desliga o site):
  veja a lista "Ajustes do Xiaomi" no README.
Backups ficam em Armazenamento interno/FiosMJ-backups: sincronize essa
pasta com o Google Drive (ou copie para o PC) toda semana.
────────────────────────────────────────────────────────────
EOF
