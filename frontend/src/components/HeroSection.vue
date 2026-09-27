<template>
  <section class="hero">

    <!-- Fundo vintage em collage -->
    <div class="hero-bg" aria-hidden="true">
      <!-- Coluna 1 -->
      <div class="bg-col col-1">
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[0]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[3]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[0]})` }"></div>
      </div>
      <!-- Coluna 2 -->
      <div class="bg-col col-2">
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[1]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[4]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[1]})` }"></div>
      </div>
      <!-- Coluna 3 -->
      <div class="bg-col col-3">
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[2]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[5]})` }"></div>
        <div class="bg-tile" :style="{ backgroundImage: `url(${imgs[2]})` }"></div>
      </div>
      <!-- Overlay vintage -->
      <div class="vintage-overlay"></div>
      <!-- Grain texture -->
      <div class="grain"></div>
    </div>

    <!-- Conteúdo -->
    <div class="container hero-inner">
      <div class="hero-content fade-in">
        <h1 class="hero-title">Fios MJ</h1>
        <p class="hero-tagline">💗 Crochê artesanal | Mãe & Filha 🧶</p>
        <p class="hero-subtitle">Peças sob encomenda feitas com amor e dedicação 🎁</p>
        <div class="hero-actions">
          <a href="#produtos" class="btn-pink">✨ Ver Produtos</a>
          <a href="https://wa.me/5533999892409?text=Olá! Vi o site e gostaria de saber mais sobre as peças de crochê 🧶"
             target="_blank" rel="noopener" class="btn-whatsapp">
            💬 Fale Conosco
          </a>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="yarn-ball">🧶</div>
      </div>
    </div>

  </section>
</template>

<script>
export default {
  name: 'HeroSection',
  data() {
    return {
      imgs: [
        '/uploads/instagram/ba02686e.jpg',
        '/uploads/instagram/4e88a377.webp',
        '/uploads/instagram/af5627a6.webp',
        '/uploads/instagram/4fa9c66b.webp',
        '/uploads/instagram/2f479e1e.jpg',
        '/uploads/instagram/1ed51d7e.jpg',
      ]
    }
  }
}
</script>

<style scoped>
/* ── Secção hero ── */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding-top: 90px;
  padding-bottom: 40px;
  overflow: hidden;
  position: relative;
}

/* ── Fundo collage ── */
.hero-bg {
  position: absolute;
  inset: 0;
  display: flex;
  gap: 0;
  overflow: hidden;
  z-index: 0;
}

.bg-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow: hidden;
  background-color: #8B4A44;
}

/* Cada coluna desliza a velocidade ligeiramente diferente */
.col-1 { animation: drift-up   18s linear infinite; }
.col-2 { animation: drift-down 22s linear infinite; }
.col-3 { animation: drift-up   26s linear infinite; }

.bg-tile {
  flex: none;
  background-size: cover;
  background-position: center;
  height: 50vh;
  /* Ken Burns suave */
  animation: ken-burns 10s ease-in-out infinite alternate;
  /* Filtro vintage: sépia + desfoque */
  filter: sepia(0.55) blur(4px) brightness(0.75) saturate(0.7);
  background-color: #8B4A44; /* fallback se imagem não carrega */
}

.bg-col:nth-child(2) .bg-tile { animation-delay: -3s; }
.bg-col:nth-child(3) .bg-tile { animation-delay: -6s; }

/* ── Overlay rosa/escuro ── */
.vintage-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    160deg,
    rgba(139, 74, 68, 0.65) 0%,
    rgba(196, 115, 106, 0.50) 50%,
    rgba(196, 149, 106, 0.35) 100%
  );
  z-index: 1;
}

/* ── Grain (ruído vintage) ── */
.grain {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  z-index: 2;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 150px;
  animation: grain-shift 0.5s steps(1) infinite;
}

/* ── Animações ── */
@keyframes drift-up {
  from { transform: translateY(0); }
  to   { transform: translateY(-8%); }
}
@keyframes drift-down {
  from { transform: translateY(-8%); }
  to   { transform: translateY(0); }
}
@keyframes ken-burns {
  from { transform: scale(1.0); }
  to   { transform: scale(1.08); }
}
@keyframes grain-shift {
  0%   { background-position: 0 0; }
  25%  { background-position: 30px 15px; }
  50%  { background-position: -20px 40px; }
  75%  { background-position: 10px -10px; }
  100% { background-position: 0 0; }
}

/* ── Conteúdo sobreposto ── */
.hero-inner {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 4rem;
  color: #fff;
  text-shadow: 0 2px 16px rgba(0,0,0,0.35);
  margin-bottom: 12px;
  line-height: 1.1;
}

.hero-tagline {
  font-size: 1.4rem;
  color: #ffe0ef;
  margin-bottom: 8px;
  font-weight: 500;
  text-shadow: 0 1px 8px rgba(0,0,0,0.3);
}

.hero-subtitle {
  font-size: 1.15rem;
  color: rgba(255,255,255,0.85);
  margin-bottom: 32px;
  text-shadow: 0 1px 6px rgba(0,0,0,0.25);
}

.hero-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-decoration { flex-shrink: 0; }

.yarn-ball {
  font-size: 12rem;
  animation: pulse 3s ease-in-out infinite;
  filter: drop-shadow(0 10px 30px rgba(233, 30, 123, 0.5));
}

@media (max-width: 768px) {
  .hero-title { font-size: 2.4rem; }
  .hero-tagline { font-size: 1.05rem; }
  .hero-subtitle { font-size: 1rem; margin-bottom: 24px; }
  .hero-inner {
    flex-direction: column;
    text-align: center;
    gap: 24px;
    justify-content: center;
  }
  .hero-content { max-width: 100%; }
  .hero-actions { justify-content: center; }
  .yarn-ball { font-size: 5rem; }
  .bg-col.col-3 { display: none; }
}
</style>
