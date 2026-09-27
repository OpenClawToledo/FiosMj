<template>
  <div class="product-card" @click="handleCardClick">
    <div class="product-image-wrap">
      <img :src="product.imageUrl" :alt="product.name" class="product-image" loading="lazy" />
      <span class="product-category">{{ product.category }}</span>
      <span v-if="product.stock === 0" class="stock-badge out">Esgotado</span>
      <span v-else-if="product.stock <= 3" class="stock-badge low">⚡ Últimas unidades!</span>
    </div>
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      <p class="product-desc">{{ product.description }}</p>

      <!-- Sizes display -->
      <div v-if="product.sizes && product.sizes.length" class="product-sizes">
        <span v-for="s in product.sizes" :key="s.size" class="size-badge">
          {{ s.size }} — R${{ s.price.toFixed(0) }}
        </span>
      </div>
      <div v-else class="product-price">
        R$ {{ product.price.toFixed(2).replace('.', ',') }}
      </div>

      <!-- Size selector for products with sizes -->
      <div v-if="product.sizes && product.sizes.length" class="size-select-wrap" @click.stop>
        <label class="size-select-label">Tamanho para compra:</label>
        <select v-model="selectedSize" class="size-select">
          <option value="" disabled>Selecione o tamanho</option>
          <option v-for="s in product.sizes" :key="s.size" :value="s">
            {{ s.size }} — R${{ s.price.toFixed(2).replace('.', ',') }}
          </option>
        </select>
      </div>

      <!-- WhatsApp button -->
      <a :href="whatsappUrl" target="_blank" rel="noopener" class="btn-whatsapp btn-order" @click.stop>
        💬 Pedir pelo WhatsApp
      </a>

      <!-- Add to cart button -->
      <button
        class="btn-buy btn-order"
        :class="{ added: justAdded, disabled: product.stock === 0 }"
        :disabled="product.stock === 0"
        @click.stop="handleAddToCart"
      >
        {{ product.stock === 0 ? '❌ Esgotado' : justAdded ? 'Adicionado! ✓' : '🛍️ Adicionar ao carrinho' }}
      </button>
    </div>
  </div>
</template>

<script>
import { useCart } from '../store/cart.js'

export default {
  name: 'ProductCard',
  props: {
    product: { type: Object, required: true }
  },
  emits: ['open-product'],
  data() {
    return {
      selectedSize: '',
      justAdded: false
    }
  },
  computed: {
    whatsappUrl() {
      const name = this.product.name.replace(/[\u{1F000}-\u{1FFFF}]/gu, '').trim()
      const msg = encodeURIComponent(`Olá! Tenho interesse no produto: ${name}. Pode me dar mais informações? 🧶`)
      return `https://wa.me/5533999892409?text=${msg}`
    }
  },
  methods: {
    handleCardClick() {
      this.$emit('open-product', this.product)
    },
    handleAddToCart() {
      if (this.product.stock === 0) return
      if (this.product.sizes && this.product.sizes.length && !this.selectedSize) {
        alert('Por favor, selecione um tamanho antes de adicionar ao carrinho.')
        return
      }
      const { addItem } = useCart()
      addItem(this.product, 1, this.selectedSize || null)
      this.justAdded = true
      setTimeout(() => { this.justAdded = false }, 1500)
    }
  }
}
</script>

<style scoped>
.product-card {
  background: white;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: var(--transition);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
}

.product-image-wrap {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-category {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--pink-primary);
  color: white;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.stock-badge {
  position: absolute;
  bottom: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 700;
}
.stock-badge.out { background: #ff5252; color: white; }
.stock-badge.low { background: #ff9800; color: white; }

.product-card { cursor: pointer; }

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 1.3rem;
  color: var(--pink-dark);
  margin-bottom: 8px;
}

.product-desc {
  color: var(--text-light);
  font-size: 0.9rem;
  margin-bottom: 14px;
  line-height: 1.5;
}

.product-sizes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.size-badge {
  background: var(--pink-light);
  color: var(--pink-dark);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.product-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--pink-primary);
  margin-bottom: 16px;
  font-family: 'Playfair Display', serif;
}

.size-select-wrap {
  margin-bottom: 14px;
}

.size-select-label {
  display: block;
  font-size: 0.85rem;
  color: var(--text-light);
  margin-bottom: 6px;
  font-weight: 600;
}

.size-select {
  width: 100%;
  padding: 8px 12px;
  border: 2px solid var(--pink-light);
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--pink-dark);
  background: white;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.size-select:focus {
  border-color: var(--pink-primary);
}

.btn-order {
  display: block;
  width: 100%;
  text-align: center;
  padding: 12px;
  font-size: 0.95rem;
  margin-bottom: 10px;
  border-radius: var(--radius, 8px);
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s, transform 0.1s, background 0.2s;
}

.btn-whatsapp {
  background: #25d366;
  color: white;
}

.btn-whatsapp:hover {
  opacity: 0.9;
}

.btn-buy {
  background: #e91e7b;
  color: white;
  border: none;
  cursor: pointer;
  font-family: inherit;
}

.btn-buy:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-buy.added {
  background: #2ecc71;
}

.btn-buy.disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
