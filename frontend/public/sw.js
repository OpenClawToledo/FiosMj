/* Service worker da Fios MJ — deixa a loja e o painel instaláveis e rápidos.
 * Regras:
 *  - Nunca guarda em cache: /api (exceto vitrine pública), /admin, checkout, páginas de pagamento.
 *  - Páginas: sempre tenta a rede primeiro; sem internet, mostra a última versão salva.
 *  - Arquivos com hash (/assets) e imagens: cache primeiro, atualizando ao fundo.
 */
const VERSION = 'fiosmj-v1';
const SHELL = `${VERSION}-shell`;
const RUNTIME = `${VERSION}-runtime`;
const IMAGES = `${VERSION}-img`;
const MAX_IMAGES = 120;

const PRECACHE = ['/', '/manifest.webmanifest', '/icons/icon-192.png', '/icons/maskable-512.png'];
const PUBLIC_API = ['/api/products', '/api/site', '/api/blog'];
const NEVER = [/^\/api\/(?!products|site|blog)/, /^\/admin/, /^\/obrigado/, /^\/pendente/, /^\/api\/checkout/];

const OFFLINE_HTML = `<!doctype html><html lang="pt-BR"><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>Sem conexão · Fios MJ</title>
<body style="font-family:system-ui,sans-serif;background:#FBF6EE;color:#5a3d2b;display:grid;place-items:center;min-height:100vh;margin:0;text-align:center;padding:24px">
<div><div style="font-size:48px">🧶</div><h1 style="font-size:1.3rem">Você está sem internet</h1>
<p>Assim que a conexão voltar, é só tocar no botão.</p>
<button onclick="location.reload()" style="background:#E91E7B;color:#fff;border:0;border-radius:10px;padding:12px 22px;font-size:1rem">Tentar de novo</button></div></body></html>`;

self.addEventListener('install', event => {
  event.waitUntil(caches.open(SHELL).then(c => c.addAll(PRECACHE)).catch(() => {}));
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter(k => !k.startsWith(VERSION)).map(k => caches.delete(k)));
    await self.clients.claim();
  })());
});

async function trim(cacheName, max) {
  const cache = await caches.open(cacheName);
  const keys = await cache.keys();
  for (let i = 0; i < keys.length - max; i++) await cache.delete(keys[i]);
}

async function networkFirst(request, cacheName, fallback) {
  try {
    const res = await fetch(request);
    if (res.ok) {
      const copy = res.clone();
      caches.open(cacheName).then(c => c.put(request, copy)).catch(() => {});
    }
    return res;
  } catch (_) {
    const cached = await caches.match(request);
    if (cached) return cached;
    if (fallback) return fallback();
    throw _;
  }
}

async function staleWhileRevalidate(request, cacheName, max) {
  const cache = await caches.open(cacheName);
  const cached = await cache.match(request);
  const network = fetch(request).then(res => {
    if (res.ok) { cache.put(request, res.clone()); if (max) trim(cacheName, max); }
    return res;
  }).catch(() => cached);
  return cached || network;
}

self.addEventListener('fetch', event => {
  const { request } = event;
  if (request.method !== 'GET') return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;           // Mercado Pago, fontes, Instagram: direto da rede
  if (NEVER.some(re => re.test(url.pathname))) return;      // dados pessoais e pagamento: nunca em cache

  // Páginas (loja, produto, blog, termos)
  if (request.mode === 'navigate') {
    event.respondWith(networkFirst(request, RUNTIME, async () =>
      (await caches.match('/')) || new Response(OFFLINE_HTML, { headers: { 'Content-Type': 'text/html; charset=utf-8' } })));
    return;
  }

  // Vitrine pública: rede primeiro, cache se estiver sem internet
  if (PUBLIC_API.some(p => url.pathname.startsWith(p))) {
    event.respondWith(networkFirst(request, RUNTIME));
    return;
  }

  // Arquivos do app (nome com hash não muda)
  if (url.pathname.startsWith('/assets/')) {
    event.respondWith(caches.match(request).then(c => c || networkFirst(request, RUNTIME)));
    return;
  }

  // Fotos e ícones
  if (/^\/(img|uploads|icons|api\/img)\//.test(url.pathname) || /\.(png|jpe?g|webp|svg)$/i.test(url.pathname)) {
    event.respondWith(staleWhileRevalidate(request, IMAGES, MAX_IMAGES));
  }
});
