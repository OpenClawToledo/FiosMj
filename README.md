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
