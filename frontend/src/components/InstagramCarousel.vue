<template>
  <section class="instagram-section">
    <div class="section-header">
      <span class="ig-logo">📸</span>
      <div>
        <h2 class="section-title">Siga a gente no Instagram</h2>
        <a class="ig-handle" href="https://instagram.com/fiosmjcroche" target="_blank" rel="noopener">
          @fiosmjcroche
        </a>
      </div>
    </div>

    <div v-if="posts.length === 0" class="ig-placeholder">
      <div class="ig-grid-placeholder">
        <a href="https://instagram.com/fiosmjcroche" target="_blank" class="ig-cta-block">
          <span class="ig-icon-big">📸</span>
          <p>Veja nossos trabalhos no Instagram!</p>
          <span class="ig-btn">@fiosmjcroche →</span>
        </a>
      </div>
    </div>

    <div v-else class="carousel-wrapper">
      <button class="carousel-arrow left" @click="prev" aria-label="Anterior">‹</button>

      <div class="carousel-track" ref="track">
        <div
          v-for="(post, i) in posts"
          :key="post.id"
          class="carousel-slide"
          :class="{ active: i === current }"
        >
          <a :href="post.postUrl || 'https://instagram.com/fiosmjcroche'" target="_blank" rel="noopener" class="ig-card">
            <div class="ig-img-wrap">
              <img :src="proxyUrl(post.imageUrl)" :alt="post.caption" loading="lazy" />
              <div class="ig-overlay">
                <span class="ig-view">Ver no Instagram ↗</span>
              </div>
            </div>
            <p class="ig-caption">{{ post.caption }}</p>
          </a>
        </div>
      </div>

      <button class="carousel-arrow right" @click="next" aria-label="Próximo">›</button>

      <!-- Dots -->
      <div class="carousel-dots">
        <button
          v-for="(_, i) in posts"
          :key="i"
          class="dot"
          :class="{ active: i === current }"
          @click="current = i"
        />
      </div>
    </div>

    <div class="ig-follow-cta">
      <a href="https://instagram.com/fiosmjcroche" target="_blank" rel="noopener" class="btn-follow">
        📸 Seguir @fiosmjcroche
      </a>
    </div>
  </section>
</template>

<script>
export default {
  name: 'InstagramCarousel',
  data() {
    return {
      posts: [],
      current: 0,
      timer: null
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/social/instagram')
      if (res.ok) this.posts = await res.json()
    } catch {}
    if (this.posts.length > 1) {
      this.timer = setInterval(() => this.next(), 4000)
    }
  },
  beforeUnmount() {
    clearInterval(this.timer)
  },
  methods: {
    next() { this.current = (this.current + 1) % this.posts.length },
    prev() { this.current = (this.current - 1 + this.posts.length) % this.posts.length },
    proxyUrl(url) {
      if (!url) return ''
      // Se já é URL local (/uploads/...) ou URL do nosso domínio, usa directo
      if (url.startsWith('/') || url.startsWith('https://fiosmj.com')) return url
      // Caso contrário, passa pelo proxy do servidor para evitar bloqueio do Instagram
      return '/api/img/proxy?url=' + encodeURIComponent(url)
    }
  }
}
</script>

<style scoped>
.instagram-section {
  padding: 60px 20px;
  background: linear-gradient(135deg, #fff5fb 0%, #fce4f0 100%);
  text-align: center;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 36px;
}

.ig-logo { font-size: 2.5rem; }

.section-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px;
  font-family: 'Playfair Display', serif;
}

.ig-handle {
  color: #e91e7b;
  font-weight: 600;
  font-size: 1rem;
  text-decoration: none;
}
.ig-handle:hover { text-decoration: underline; }

/* Placeholder */
.ig-grid-placeholder {
  max-width: 400px;
  margin: 0 auto 24px;
}

.ig-cta-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  background: white;
  border-radius: 20px;
  text-decoration: none;
  box-shadow: 0 4px 24px rgba(233,30,123,0.1);
  transition: transform 0.2s;
}
.ig-cta-block:hover { transform: translateY(-4px); }
.ig-icon-big { font-size: 3rem; }
.ig-cta-block p { color: #555; font-size: 1rem; margin: 0; }
.ig-btn { background: #e91e7b; color: white; padding: 10px 24px; border-radius: 20px; font-weight: 700; font-size: 0.9rem; }

/* Carousel */
.carousel-wrapper {
  position: relative;
  max-width: 360px;
  margin: 0 auto 24px;
}

.carousel-track {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
}

.carousel-slide {
  display: none;
}
.carousel-slide.active {
  display: block;
}

.ig-card {
  display: block;
  text-decoration: none;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(233,30,123,0.12);
}

.ig-img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.ig-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.ig-card:hover .ig-img-wrap img { transform: scale(1.05); }

.ig-overlay {
  position: absolute;
  inset: 0;
  background: rgba(233,30,123,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.ig-card:hover .ig-overlay { opacity: 1; }
.ig-view { color: white; font-weight: 700; font-size: 1rem; }

.ig-caption {
  padding: 12px 16px;
  font-size: 0.85rem;
  color: #555;
  margin: 0;
  text-align: left;
  line-height: 1.4;
}

/* Arrows */
.carousel-arrow {
  position: absolute;
  top: 40%;
  transform: translateY(-50%);
  background: white;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 1.4rem;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
  z-index: 10;
  color: #e91e7b;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.carousel-arrow.left { left: -18px; }
.carousel-arrow.right { right: -18px; }
.carousel-arrow:hover { background: #e91e7b; color: white; }

/* Dots */
.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f0c0d8;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
  padding: 0;
}
.dot.active { background: #e91e7b; }

/* CTA */
.ig-follow-cta { margin-top: 8px; }
.btn-follow {
  display: inline-block;
  background: linear-gradient(135deg, #e91e7b, #c2185b);
  color: white;
  padding: 12px 28px;
  border-radius: 24px;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.95rem;
  box-shadow: 0 4px 16px rgba(233,30,123,0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}
.btn-follow:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(233,30,123,0.4);
}

@media (max-width: 480px) {
  .section-header { flex-direction: column; gap: 8px; }
  .section-title { font-size: 1.3rem; }
}
</style>
