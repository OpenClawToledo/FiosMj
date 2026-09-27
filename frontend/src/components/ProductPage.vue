<template>
  <div class="product-page">
    <div class="container">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <a href="#" @click.prevent="$emit('go-home')">🏠 Início</a>
        <span class="sep">›</span>
        <a href="#produtos" @click.prevent="$emit('go-home')">Produtos</a>
        <span class="sep">›</span>
        <span>{{ product.name }}</span>
      </nav>

      <div class="product-layout">
        <!-- Image -->
        <div class="product-image-side">
          <div class="main-image-wrap">
            <img :src="product.imageUrl" :alt="product.name" class="main-image" />
            <span v-if="product.stock === 0" class="stock-badge out">Esgotado</span>
            <span v-else-if="product.stock <= 3" class="stock-badge low">⚡ Últimas unidades!</span>
          </div>
        </div>

        <!-- Info -->
        <div class="product-info-side">
          <span class="product-category-tag">{{ product.category }}</span>
          <h1 class="product-title">{{ product.name }}</h1>

          <!-- Price -->
          <div v-if="product.sizes && product.sizes.length" class="product-price-wrap">
            <span class="price-from">A partir de</span>
            <span class="product-price">R$ {{ minPrice }}</span>
          </div>
          <div v-else class="product-price-wrap">
            <span class="product-price">R$ {{ product.price.toFixed(2).replace('.', ',') }}</span>
          </div>

          <p class="product-description">{{ product.description }}</p>

          <!-- Sizes -->
          <div v-if="product.sizes && product.sizes.length" class="size-selector">
            <label class="size-label">Tamanho:</label>
            <div class="size-options">
              <button
                v-for="s in product.sizes"
                :key="s.size"
                class="size-btn"
                :class="{ selected: selectedSize?.size === s.size }"
                @click="selectedSize = s"
              >
                {{ s.size }}<br/>
                <small>R${{ s.price.toFixed(0) }}</small>
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="product-actions">
            <button
              class="btn-add-cart"
              :disabled="product.stock === 0"
              :class="{ added: justAdded, disabled: product.stock === 0 }"
              @click="handleAddToCart"
            >
              {{ product.stock === 0 ? '❌ Esgotado' : justAdded ? '✓ Adicionado!' : '🛍️ Adicionar ao Carrinho' }}
            </button>
            <a :href="whatsappUrl" target="_blank" rel="noopener" class="btn-whatsapp">
              💬 Pedir pelo WhatsApp
            </a>
          </div>

          <!-- Stock info -->
          <div v-if="product.stock && product.stock > 3 && product.stock < 10" class="stock-info">
            📦 {{ product.stock }} unidades disponíveis
          </div>
        </div>
      </div>

      <!-- Reviews -->
      <div class="reviews-section">
        <h2 class="reviews-title">⭐ Avaliações do Produto</h2>
        <div v-if="loadingReviews" class="reviews-loading">Carregando avaliações...</div>
        <div v-else-if="reviews.length === 0" class="reviews-empty">
          <p>Ainda não há avaliações para este produto.</p>
          <p>Seja o primeiro! 💕</p>
        </div>
        <div v-else class="reviews-grid">
          <div v-for="r in reviews" :key="r.id" class="review-card">
            <div class="review-header">
              <span class="review-name">{{ r.customerName }}</span>
              <span class="review-city" v-if="r.city">📍 {{ r.city }}</span>
              <span class="review-stars">{{ '⭐'.repeat(r.rating || 5) }}</span>
            </div>
            <p class="review-message">{{ r.message }}</p>
          </div>
        </div>
        <div class="review-cta">
          <p>Comprou este produto? Deixe sua avaliação! 💬</p>
          <a
            :href="`https://wa.me/5533999892409?text=${encodeURIComponent('Olá! Quero deixar uma avaliação do produto: ' + product.name)}`"
            target="_blank" rel="noopener"
            class="btn-review"
          >✍️ Deixar Avaliação</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useCart } from '../store/cart.js'

export default {
  name: 'ProductPage',
  props: {
    product: { type: Object, required: true }
  },
  emits: ['go-home'],
  data() {
    return {
      selectedSize: null,
      justAdded: false,
      reviews: [],
      loadingReviews: true
    }
  },
  computed: {
    minPrice() {
      if (!this.product.sizes || !this.product.sizes.length) return ''
      const min = Math.min(...this.product.sizes.map(s => s.price))
      return min.toFixed(2).replace('.', ',')
    },
    whatsappUrl() {
      const name = this.product.name.replace(/[\u{1F000}-\u{1FFFF}]/gu, '').trim()
      const msg = encodeURIComponent(`Olá! Tenho interesse no produto: ${name}. Pode me dar mais informações? 🧶`)
      return `https://wa.me/5533999892409?text=${msg}`
    }
  },
  methods: {
    handleAddToCart() {
      if (this.product.sizes && this.product.sizes.length && !this.selectedSize) {
        alert('Por favor, selecione um tamanho.')
        return
      }
      const { addItem } = useCart()
      addItem(this.product, 1, this.selectedSize || null)
      this.justAdded = true
      setTimeout(() => { this.justAdded = false }, 1500)
    },
    async loadReviews() {
      this.loadingReviews = true
      try {
        const res = await fetch(`/api/social/testimonials?productId=${this.product.id}`)
        if (res.ok) this.reviews = await res.json()
      } catch {}
      this.loadingReviews = false
    }
  },
  mounted() {
    this.loadReviews()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
</script>

<style scoped>
.product-page {
  padding: 100px 0 60px;
  min-height: 100vh;
  background: #fff5fb;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
  color: #888;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.breadcrumb a {
  color: #e91e7b;
  font-weight: 600;
}

.breadcrumb a:hover { text-decoration: underline; }
.sep { color: #ccc; }

.product-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  margin-bottom: 60px;
}

.main-image-wrap {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  aspect-ratio: 1;
  box-shadow: 0 8px 40px rgba(233,30,123,0.15);
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stock-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
}

.stock-badge.out { background: #ff5252; color: white; }
.stock-badge.low { background: #ff9800; color: white; }

.product-category-tag {
  display: inline-block;
  background: #fce4f0;
  color: #c2185b;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 0.82rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.product-title {
  font-size: 2rem;
  color: #1a1a2e;
  margin-bottom: 16px;
  font-family: 'Playfair Display', serif;
  line-height: 1.2;
}

.product-price-wrap {
  margin-bottom: 20px;
}

.price-from {
  font-size: 0.85rem;
  color: #888;
  display: block;
  margin-bottom: 4px;
}

.product-price {
  font-size: 2.2rem;
  font-weight: 700;
  color: #e91e7b;
  font-family: 'Playfair Display', serif;
}

.product-description {
  color: #555;
  line-height: 1.7;
  margin-bottom: 24px;
  font-size: 1rem;
}

.size-selector { margin-bottom: 24px; }

.size-label {
  display: block;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.size-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.size-btn {
  padding: 10px 18px;
  border: 2px solid #fce4f0;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  text-align: center;
  transition: all 0.2s;
  line-height: 1.3;
}

.size-btn:hover { border-color: #e91e7b; }
.size-btn.selected {
  background: #e91e7b;
  border-color: #e91e7b;
  color: white;
}

.size-btn small { font-size: 0.75rem; font-weight: 400; }

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.btn-add-cart {
  padding: 14px;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
  text-align: center;
}

.btn-add-cart:hover:not(.disabled) { opacity: 0.9; transform: translateY(-1px); }
.btn-add-cart.added { background: #2ecc71; }
.btn-add-cart.disabled { background: #ccc; cursor: not-allowed; }

.btn-whatsapp {
  display: block;
  padding: 14px;
  background: #25d366;
  color: white;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 700;
  text-decoration: none;
  text-align: center;
  transition: opacity 0.2s;
}

.btn-whatsapp:hover { opacity: 0.9; }

.stock-info {
  font-size: 0.88rem;
  color: #888;
  padding: 8px 12px;
  background: #fff3e0;
  border-radius: 8px;
  display: inline-block;
}

/* Reviews */
.reviews-section {
  border-top: 2px solid #fce4f0;
  padding-top: 48px;
}

.reviews-title {
  font-size: 1.5rem;
  color: #1a1a2e;
  margin-bottom: 24px;
  font-family: 'Playfair Display', serif;
}

.reviews-loading, .reviews-empty {
  text-align: center;
  color: #aaa;
  padding: 32px;
}

.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.review-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(233,30,123,0.08);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.review-name { font-weight: 700; color: #333; }
.review-city { font-size: 0.8rem; color: #888; }
.review-stars { font-size: 0.85rem; margin-left: auto; }
.review-message { color: #555; font-size: 0.9rem; line-height: 1.6; }

.review-cta {
  text-align: center;
  padding: 24px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(233,30,123,0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.btn-review {
  display: inline-block;
  background: #e91e7b;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  transition: opacity 0.2s;
}

.btn-review:hover { opacity: 0.9; }

@media (max-width: 768px) {
  .product-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .product-title { font-size: 1.5rem; }
}
</style>
