<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="footer-brand">
        <div class="footer-logo">🧶 <span>Fios MJ</span></div>
        <p class="footer-bio" v-html="$site.rich($site.s.footerBio)"></p>
      </div>

      <div class="footer-nav">
        <h4>Navegação</h4>
        <a href="#produtos">Produtos</a>
        <a href="#como-pedir">Como Pedir</a>
        <a href="#sobre">Sobre Nós</a>
        <a href="#blog">Blog</a>
      </div>

      <div class="footer-legal">
        <h4>Informações</h4>
        <a href="/politica-de-privacidade" target="_blank" rel="noopener">🔒 Privacidade</a>
        <a href="/termos-e-condicoes" target="_blank" rel="noopener">📋 Termos e Condições</a>
        <a href="/contacto" target="_blank" rel="noopener">💌 Contato</a>
      </div>

      <div class="footer-contact">
        <h4>Contato</h4>
        <a :href="$site.wa()" target="_blank" rel="noopener">
          💬 {{ $site.s.whatsappDisplay }}
        </a>
        <a :href="$site.igUrl" target="_blank" rel="noopener">
          📸 {{ $site.igHandle }}
        </a>
        <a v-if="$site.s.facebook" :href="$site.s.facebook" target="_blank" rel="noopener">
          👍 Facebook
        </a>
        <span class="location" v-if="$site.s.location">📍 {{ $site.s.location }}</span>
      </div>
    </div>

    <div class="footer-bottom">
      <p>{{ $site.s.footerCopyright }}</p>
      <p class="footer-id">
        {{ $site.s.lojaNome }}<template v-if="$site.s.lojaDocumento"> · CNPJ/CPF {{ $site.s.lojaDocumento }}</template><template v-if="$site.s.lojaEndereco"> · {{ $site.s.lojaEndereco }}</template><template v-if="$site.s.lojaEmail"> · <a :href="'mailto:' + $site.s.lojaEmail">{{ $site.s.lojaEmail }}</a></template>
      </p>
    </div>
  </footer>
</template>

<script>
export default { name: 'AppFooter' }
</script>

<style scoped>
.footer {
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.8);
}

.footer-inner {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 40px;
  padding: 50px 20px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-logo {
  font-size: 1.8rem;
  margin-bottom: 14px;
}

.footer-logo span {
  font-family: 'Playfair Display', Georgia, serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f48fb1;
  margin-left: 6px;
}

.footer-bio {
  font-size: 0.95rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.55);
}

.footer-nav,
.footer-contact,
.footer-legal {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-nav h4,
.footer-contact h4,
.footer-legal h4 {
  font-family: 'Playfair Display', Georgia, serif;
  font-size: 1.1rem;
  color: #f48fb1;
  margin-bottom: 4px;
}

.footer-nav a,
.footer-contact a {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.95rem;
  transition: color 0.2s;
}

.footer-nav a:hover,
.footer-contact a:hover,
.footer-legal a:hover {
  color: #f48fb1;
}

.footer-legal a {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.95rem;
  transition: color 0.2s;
}

.location {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.45);
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 20px;
  text-align: center;
}

.footer-bottom p {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: 30px;
    text-align: center;
    padding: 40px 20px 30px;
  }

  .footer-nav,
  .footer-contact {
    align-items: center;
  }
}

.footer-id {
  font-size: 0.78rem;
  opacity: 0.7;
  margin-top: 6px;
}
.footer-id a { color: inherit; }
</style>
