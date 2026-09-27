<template>
  <section class="gifts-section" v-if="gifts.length">
    <div class="section-inner">
      <h2 class="section-title">🎁 Presenteou com Amor</h2>
      <p class="section-sub">Momentos especiais celebrados com crochê artesanal ✨</p>

      <div class="gifts-carousel">
        <div class="gifts-track" :style="{ transform: `translateX(-${current * 100}%)` }">
          <div v-for="g in gifts" :key="g.id" class="gift-slide">
            <div class="gift-card">
              <div v-if="g.imageUrl" class="gift-img">
                <img :src="g.imageUrl" :alt="g.customerName" />
              </div>
              <div class="gift-heart">💕</div>
              <div class="gift-occasion" v-if="g.occasion">
                🎁 {{ g.occasion }}
              </div>
              <p class="gift-message">"{{ g.message }}"</p>
              <div class="gift-author">
                <span class="gift-name">{{ g.customerName }}</span>
                <span v-if="g.city" class="gift-city">📍 {{ g.city }}</span>
              </div>
              <div class="gift-stars">{{ '⭐'.repeat(g.rating || 5) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <div class="gifts-nav" v-if="gifts.length > 1">
        <button @click="prev" class="nav-btn">‹</button>
        <div class="dots">
          <span
            v-for="(_, i) in gifts" :key="i"
            class="dot" :class="{ active: i === current }"
            @click="current = i"
          />
        </div>
        <button @click="next" class="nav-btn">›</button>
      </div>

      <!-- CTA -->
      <div class="gift-cta">
        <p>✨ Você também pode fazer parte! Presenteie alguém especial com crochê artesanal.</p>
        <a href="#produtos" class="btn-gift">Ver produtos para presentear →</a>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'GiftShowcase',
  data() {
    return {
      gifts: [],
      current: 0,
      timer: null
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/social/gifts')
      if (res.ok) this.gifts = await res.json()
    } catch {}
    if (this.gifts.length > 1) {
      this.timer = setInterval(() => this.next(), 5000)
    }
  },
  beforeUnmount() {
    clearInterval(this.timer)
  },
  methods: {
    next() { this.current = (this.current + 1) % this.gifts.length },
    prev() { this.current = (this.current - 1 + this.gifts.length) % this.gifts.length }
  }
}
</script>

<style scoped>
.gifts-section {
  padding: 60px 20px;
  background: white;
  overflow: hidden;
}

.section-inner {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.section-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 8px;
}

.section-sub {
  color: #888;
  margin-bottom: 36px;
  font-size: 0.95rem;
}

/* Carousel */
.gifts-carousel {
  overflow: hidden;
  border-radius: 20px;
  margin-bottom: 20px;
}

.gifts-track {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.gift-slide {
  min-width: 100%;
  padding: 0 4px;
}

.gift-card {
  background: linear-gradient(135deg, #fff5fb, #fce4f0);
  border-radius: 20px;
  padding: 36px 32px;
  border: 1px solid #f5c0d8;
  position: relative;
}

.gift-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
  border: 3px solid #e91e7b;
}
.gift-img img { width: 100%; height: 100%; object-fit: cover; }

.gift-heart {
  font-size: 2rem;
  margin-bottom: 12px;
}

.gift-occasion {
  display: inline-block;
  background: #e91e7b;
  color: white;
  font-size: 0.82rem;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.gift-message {
  font-size: 1rem;
  color: #444;
  font-style: italic;
  line-height: 1.7;
  margin-bottom: 16px;
}

.gift-author {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.gift-name {
  font-weight: 700;
  color: #e91e7b;
  font-size: 0.95rem;
}

.gift-city {
  color: #999;
  font-size: 0.82rem;
}

.gift-stars { font-size: 0.9rem; }

/* Nav */
.gifts-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.nav-btn {
  background: white;
  border: 2px solid #e91e7b;
  color: #e91e7b;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 1.3rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.nav-btn:hover { background: #e91e7b; color: white; }

.dots { display: flex; gap: 6px; }
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f0c0d8;
  cursor: pointer;
  transition: background 0.2s;
}
.dot.active { background: #e91e7b; }

/* CTA */
.gift-cta {
  background: #fff5fb;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #fce4f0;
}
.gift-cta p {
  color: #666;
  font-size: 0.92rem;
  margin-bottom: 14px;
}
.btn-gift {
  display: inline-block;
  background: #e91e7b;
  color: white;
  padding: 12px 24px;
  border-radius: 24px;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.9rem;
  transition: opacity 0.2s;
}
.btn-gift:hover { opacity: 0.9; }
</style>
