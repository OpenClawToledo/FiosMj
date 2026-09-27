<template>
  <section id="produtos" class="products-section">
    <div class="container">
      <h2 class="section-title">✨ Nossos Produtos</h2>
      <p class="section-subtitle">Peças artesanais únicas, feitas sob encomenda com muito carinho</p>

      <ProductFilters :products="products" @update:filtered="filteredProducts = $event" />

      <div v-if="filteredProducts.length === 0" class="no-results">
        <span>😔</span>
        <p>Nenhum produto encontrado nessa categoria.</p>
      </div>

      <div v-else class="products-grid">
        <ProductCard
          v-for="product in filteredProducts"
          :key="product.id"
          :product="product"
          @open-product="$emit('open-product', $event)"
        />
      </div>

      <div class="products-cta">
        <p>Não encontrou o que procura? Fazemos peças personalizadas! 💬</p>
        <a
          href="https://wa.me/5533999892409?text=Olá! Gostaria de solicitar uma peça personalizada de crochê 😊"
          target="_blank"
          rel="noopener"
          class="btn-pink"
        >
          💬 Pedir Personalizado
        </a>
      </div>
    </div>
  </section>
</template>

<script>
import ProductCard from './ProductCard.vue'
import ProductFilters from './ProductFilters.vue'

export default {
  name: 'ProductGrid',
  components: { ProductCard, ProductFilters },
  props: {
    products: { type: Array, default: () => [] }
  },
  emits: ['open-product'],
  data() {
    return {
      filteredProducts: []
    }
  },
  watch: {
    products: {
      immediate: true,
      handler(val) {
        this.filteredProducts = val
      }
    }
  }
}
</script>

<style scoped>
.products-section {
  padding: 80px 0;
  background: var(--pink-bg);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.no-results {
  text-align: center;
  padding: 48px;
  color: #aaa;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 1.05rem;
}

.no-results span { font-size: 2.5rem; }

.products-cta {
  text-align: center;
  padding: 30px;
  background: var(--pink-light);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.products-cta p {
  font-size: 1.05rem;
  color: var(--text-light);
}

@media (max-width: 768px) {
  .products-section {
    padding: 50px 0;
  }

  .products-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
