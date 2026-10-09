# 🧶 Fios MJ — Loja de Crochê Artesanal

Loja online da **Fios MJ**: crochê artesanal feito por mãe e filha.
Produção: **https://fiosmj.com** · Painel: **https://fiosmj.com/admin**

**Stack:** Spring Boot 3 (Java 17) + Vue 3 (Vite) + SQLite, empacotados num único container Docker.

---

## Apps no celular

O site vira **dois apps instaláveis**, sem loja de aplicativos:

| App | Endereço | Para quem |
|---|---|---|
| **Fios MJ** (loja) | https://fiosmj.com | clientes: ver peças, carrinho e pagamento |
| **Painel MJ** | https://fiosmj.com/admin | equipe: pedidos, produtos, blog, mensagens, textos |

- **Android (Chrome):** abrir o endereço → aparece "Instalar" (ou menu ⋮ → *Instalar app*).
- **iPhone (Safari):** Compartilhar → *Adicionar à Tela de Início*.
- No painel, marque **"Manter conectada neste aparelho"** para não digitar a senha toda vez. Só faça isso no celular pessoal.

## Painel admin (`/admin`)

Entre com a senha definida em `ADMIN_SECRET`. O painel tem sete abas:

| Aba | O que dá para fazer |
|---|---|
| **Pedidos** | todos os pedidos (com ou sem conta da cliente), WhatsApp da cliente, endereço, marcar enviado/entregue/cancelado |
| **Mensagens** | mensagens do formulário de contato |
| **Produtos** | criar, editar, ocultar/mostrar, apagar; tamanhos com preço; envio de foto; disponibilidade (pronta entrega, sob encomenda, sob consulta, indisponível) e prazo de produção |
| **Blog** | criar, editar, publicar/despublicar, apagar posts; capa com envio de foto; texto simples (`## Subtítulo`, `- item`) ou HTML |
| **Depoimentos** | aprovar, aprovar com destaque ou recusar depoimentos enviados pelas clientes |
| **Newsletter** | ver inscritas e exportar CSV |
| **Site** | textos do topo, Sobre, Como encomendar, rodapé, aviso de Natal e contatos (WhatsApp, Instagram, Facebook) |

As fotos enviadas ficam em `/uploads/<categoria>/…`, no volume de dados.

### SEO e compartilhamento

- `https://fiosmj.com/produto/<id>` e `https://fiosmj.com/blog/<slug>` abrem direto no produto/post e mostram foto, título e descrição próprios quando o link é colado no WhatsApp, Instagram ou Facebook.
- `/sitemap.xml` é gerado na hora com todos os produtos visíveis e posts publicados.
- A página do produto tem o botão **Compartilhar**, e a mensagem do WhatsApp já leva o link do produto.

---

## Rodar localmente

```bash
# 1. Frontend (gera os arquivos em src/main/resources/static)
cd frontend && npm install && npm run build && cd ..

# 2. Backend
cp .env.example .env    # preencha JWT_SECRET e ADMIN_SECRET
./mvnw spring-boot:run  # http://localhost:8081
```

Com hot reload do frontend: `cd frontend && npm run dev` (http://localhost:5173, com proxy para a API em :8081).

Os dados locais ficam em `./data/` (banco `fiosmj.db` + `uploads/`), e essa pasta não vai para o git.

---

## Produção (KVM Hostinger)

```
Internet ─► Nginx do host (80/443, Certbot) ─► 127.0.0.1:8081 ─► container "fiosmj"
                                                                  └─ volume fiosmj_data
                                                                      ├─ fiosmj.db
                                                                      └─ uploads/
```

O Nginx é **o do host**, compartilhado com os outros sites da KVM. O projeto não sobe Nginx próprio.

### Primeira instalação

```bash
mkdir -p /opt/apps && cd /opt/apps
git clone https://github.com/OpenClawToledo/FiosMj.git fiosmj && cd fiosmj
cp .env.example .env && nano .env        # JWT_SECRET, ADMIN_SECRET, MP_*
docker volume create fiosmj_data >/dev/null
docker compose up -d --build
curl -s http://127.0.0.1:8081/api/health

# Nginx
cp deploy/nginx-fiosmj.conf /etc/nginx/sites-available/fiosmj
ln -sf /etc/nginx/sites-available/fiosmj /etc/nginx/sites-enabled/fiosmj
nginx -t && systemctl reload nginx
certbot --nginx -d fiosmj.com -d www.fiosmj.com
```

### Atualizar

```bash
cd /opt/apps/fiosmj && ./deploy.sh
```

O script faz `git pull`, backup do volume e rebuild, e depois testa `/api/health`.

### Backup

`scripts/backup.sh` compacta o volume em `/opt/backups/fiosmj/` e mantém 30 dias. Para agendar:

```bash
crontab -e
15 3 * * * /opt/apps/fiosmj/scripts/backup.sh >> /var/log/fiosmj-backup.log 2>&1
```

Os backups ficam **na KVM**, e o banco nunca vai para o GitHub, porque tem dados de clientes (LGPD).

### Migrar os dados da instalação antiga (OpenClaw)

```bash
docker compose stop app
./scripts/import-data.sh /caminho/antigo/fiosmj.db /caminho/antigo/uploads
docker compose up -d
```

---

## Rodar num computador de casa (grátis, com Cloudflare Tunnel)

O site fica num notebook/PC ligado 24h e sai para a internet pelo **Cloudflare Tunnel** (grátis): sem abrir portas no roteador e com HTTPS automático. Se faltar luz ou internet em casa, o site sai do ar até voltar.

**Requisitos:** computador 64 bits com 4 GB de RAM ou mais (Linux, Windows 10/11 ou Raspberry Pi 4/5 de 64 bits), ligado na tomada e na internet o tempo todo.

### 1. Cloudflare (uma vez)

1. Crie uma conta grátis em cloudflare.com → **Adicionar domínio** → `fiosmj.com` → plano **Free**.
2. A Cloudflare mostra **2 nameservers**. No painel da Hostinger: Domínios → fiosmj.com → **Nameservers** → trocar pelos 2 da Cloudflare. Espere ficar "Ativo" (minutos a algumas horas).
3. Em **DNS → Registros**, apague os registros **A** de `fiosmj.com` e `www` que apontavam para a KVM antiga (mantenha os de e-mail, se houver).
4. **Zero Trust → Networks → Tunnels → Create a tunnel → Cloudflared**, nome `fiosmj`. Na tela de instalação, copie o **token** (o texto longo depois de `--token`, começa com `eyJ`).
5. Na aba **Public Hostnames** do túnel, adicione dois:
   - Domínio `fiosmj.com`, serviço **HTTP**, URL `app:8081`
   - Subdomínio `www`, domínio `fiosmj.com`, serviço **HTTP**, URL `app:8081`
6. **SSL/TLS → Edge Certificates → Always Use HTTPS**: ligado.

### 2a. Computador com Linux (Ubuntu, Debian, Mint, Raspberry Pi OS)

```bash
curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-casa.sh | sudo bash
```

O script instala o Docker, pede a senha do painel, o token do túnel e as chaves do Mercado Pago, impede o computador de hibernar, sobe o site e agenda o backup diário.

### 2b. Computador com Windows 10/11

1. Instale o **Docker Desktop** (docker.com) e o **Git** (git-scm.com). No Docker Desktop: Settings → General → marque **Start Docker Desktop when you sign in**.
2. Configurações do Windows → Energia → **Suspender: Nunca** (na tomada).
3. No **PowerShell**:
   ```powershell
   cd $HOME
   git clone https://github.com/OpenClawToledo/FiosMj.git fiosmj
   cd fiosmj
   copy .env.example .env
   notepad .env
   ```
4. No `.env`, preencha: `JWT_SECRET` (64 letras/números aleatórios), `ADMIN_SECRET` (senha do painel, 12+ caracteres, sem espaço ou `$`), `MP_ACCESS_TOKEN`, `MP_PUBLIC_KEY`, `TUNNEL_TOKEN` e acrescente a linha `COMPOSE_FILE=docker-compose.casa.yml`. Salve.
5. Suba:
   ```powershell
   docker compose up -d --build
   ```
6. Backup (rode 1 vez por semana e guarde a pasta `backups` no Google Drive):
   ```powershell
   mkdir backups -Force; docker run --rm -v fiosmj_data:/data:ro -v ${PWD}\backups:/out alpine tar czf /out/fiosmj-$(Get-Date -f yyyy-MM-dd).tar.gz -C /data .
   ```
7. Atualizar depois: `git pull` e `docker compose up -d --build`.

### Conferir

- Neste computador: http://localhost:8081
- Na internet: https://fiosmj.com e https://fiosmj.com/admin
- Túnel: `docker compose logs tunnel` deve mostrar "Registered tunnel connection".

Sem Nginx na frente, o próprio app limita tentativas: 10 senhas erradas no painel bloqueiam o IP por 15 minutos, e checkout/contato/cadastro aceitam 10 pedidos por minuto por IP. Num servidor novo com banco vazio, os produtos iniciais são carregados sozinhos.

## Rodar num celular Android (ex.: Xiaomi 17T Pro)

Dá para usar um celular Android como servidor, pelo **Termux** (sem Docker) e pelo mesmo **Cloudflare Tunnel** da seção anterior. É a opção mais frágil: o Android pode fechar o app, e o celular fica na tomada 24h. Use como plano B ou temporário. Para a loja de verdade, prefira o Oracle Cloud grátis ou um PC.

**Requisitos:** Android 64 bits com 6 GB de RAM ou mais, Wi-Fi fixo e carregador sempre ligado. iPhone não serve.

### 1. Apps (pelo F-Droid, não pela Play Store)

Instale o **F-Droid** (f-droid.org) e, por ele, o **Termux** e o **Termux:Boot**. Abra o Termux:Boot **uma vez** e depois feche. Isso é o que faz o site voltar sozinho quando o celular reiniciar.

### 2. Cloudflare

Faça os passos de **1. Cloudflare** da seção anterior, com uma diferença: nos dois *Public Hostnames*, a URL do serviço é **`localhost:8081`** (e não `app:8081`). Se o túnel já existia para o PC, só edite os dois hostnames.

### 3. Instalar

No Termux:

```bash
curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-celular.sh | bash
```

O script instala Java, Node, Maven e cloudflared, pede a senha do painel, o token do túnel e as chaves do Mercado Pago, compila o site **no próprio celular** (de 5 a 15 minutos na primeira vez), cria os serviços que reiniciam sozinhos se caírem e agenda o backup diário. No fim, ele pede permissão de acesso aos arquivos: aceite.

### 4. Ajustes do Xiaomi (HyperOS), obrigatórios

Sem isso, o HyperOS fecha o Termux e o site sai do ar:

1. **Configurações → Apps → Gerenciar apps → Termux**: *Início automático* **ligado** e *Economia de bateria* → **Sem restrições**. Faça o mesmo para o **Termux:Boot**.
2. **Recentes** (botão quadrado): segure o card do Termux e toque no **cadeado** para travar o app.
3. Deixe a notificação do Termux aparecendo, com o texto **"wake lock held"**. Se ela sumir, abra o Termux e digite `termux-wake-lock`.
4. **Opções do desenvolvedor** (Sobre o telefone → toque 7 vezes em *Versão do SO*): ligue **Desativar restrições de processos filhos** (*Disable child process restrictions*). Se não aparecer, deixe como está.
5. **Bateria**: se houver opção de **limitar a carga em 80%** ou de *carregamento otimizado*, ligue. Isso protege a bateria de estufar com o celular sempre na tomada.
6. Wi-Fi → rede de casa → **Manter Wi-Fi ligado durante o modo de suspensão: Sempre** (se a opção existir).

### Conferir e manter

- No celular: http://localhost:8081 · Na internet: https://fiosmj.com
- Registros: `tail -f $PREFIX/var/log/sv/fiosmj/current` (site) e `tail -f $PREFIX/var/log/sv/fiosmj-tunnel/current` (túnel)
- Reiniciar o site: `sv restart fiosmj`
- **Atualizar:** rode de novo `bash ~/fiosmj/deploy/setup-celular.sh`
- **Backup:** sai todo dia às 03:15 em *Armazenamento interno/FiosMJ-backups* (30 dias). Configure o Google Drive (ou outro) para copiar essa pasta, porque, se o celular for perdido ou quebrar, os pedidos e as clientes vão junto. O arquivo tem o mesmo formato dos outros backups, e o `scripts/restore.sh` restaura num servidor normal.

## Mudar de servidor (ex.: Oracle Cloud grátis)

O app roda em Intel/AMD e em ARM. Num Ubuntu novo:

```bash
# 1. No servidor ANTIGO: gerar backup e levar o .env
cd /opt/apps/fiosmj && ./scripts/backup.sh

# 2. No servidor NOVO (copie antes o .env antigo para /home/ubuntu/fiosmj.env)
curl -fsSL https://raw.githubusercontent.com/OpenClawToledo/FiosMj/main/deploy/setup-servidor.sh | sudo bash
sudo /opt/apps/fiosmj/scripts/restore.sh /home/ubuntu/fiosmj-AAAA-MM-DD_HHMM.tar.gz

# 3. Apontar o domínio para o IP novo e, quando abrir, ativar o HTTPS
sudo certbot --nginx -d fiosmj.com -d www.fiosmj.com --redirect -m SEU_EMAIL --agree-tos -n
```

O `setup-servidor.sh` instala Docker, Nginx, Certbot e fail2ban, libera as portas 80/443 (a Oracle bloqueia por padrão), cria swap, baixa o projeto, configura o Nginx, agenda o backup diário e sobe o app.

## Variáveis de ambiente (`.env`)

| Variável | Obrigatória | Para quê |
|---|---|---|
| `JWT_SECRET` | sim | login das clientes (`openssl rand -hex 32`); mín. 32 caracteres, sem ela o app não sobe |
| `ADMIN_SECRET` | sim | senha do `/admin` (mín. 12 caracteres, sem ela o admin fica bloqueado) |
| `MP_ACCESS_TOKEN`, `MP_PUBLIC_KEY` | para vender | Mercado Pago |
| `BREVO_API_KEY`, `BREVO_LIST_ID` | não | sincronizar newsletter com a Brevo |
| `OPENCLAW_GATEWAY_URL`, `OPENCLAW_GATEWAY_TOKEN`, `MAJU_PHONE`, `TOLEDO_PHONE` | não | aviso de pedido por WhatsApp; vazio = desligado |

---

## Estrutura

```
├── Dockerfile               # build do Vue + Spring Boot numa imagem só
├── docker-compose.yml       # só o app, porta 127.0.0.1:8081
├── deploy.sh                # atualização na KVM
├── deploy/nginx-fiosmj.conf # site do Nginx do host
├── scripts/                 # backup.sh, import-data.sh
├── seed/uploads/            # fotos iniciais dos produtos (copiadas no 1º start)
├── frontend/                # Vue 3 + Vite
└── src/main/java/com/fiosmj/app/
    ├── controller/          # API pública + admin (/api/admin/**)
    ├── security/            # JWT das clientes + AdminAuth (X-Admin-Secret)
    ├── model/ repository/ service/ scheduler/
```

## Contato

WhatsApp [+55 33 99989-2409](https://wa.me/5533999892409) · Instagram [@fiosmjcroche](https://instagram.com/fiosmjcroche)

© 2026 Fios MJ
