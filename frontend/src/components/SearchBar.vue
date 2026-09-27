<template>
  <div class="search-bar" ref="searchRef">
    <div class="search-input-wrap">
      <span class="search-icon">🔍</span>
      <input
        ref="inputRef"
        v-model="query"
        type="text"
        placeholder="Buscar produtos..."
        class="search-input"
        @input="onInput"
        @keydown.esc="close"
        @focus="showDropdown = query.length >= 2"
        autocomplete="off"
      />
      <button v-if="query" class="search-clear" @click="clear">✕</button>
    </div>

    <div v-if="showDropdown && results.length > 0" class="search-dropdown">
      <div
        v-for="product in results"
        :key="product.id"
        class="search-result"
        @click="selectProduct(product)"
      >
        <img :src="product.imageUrl" :alt="product.name" class="result-img" />
        <div class="result-info">
          <span class="result-name">{{ product.name }}</span>
          <span class="result-price">R$ {{ product.price.toFixed(2).replace('.', ',') }}</span>
        </div>
        <span class="result-cat">{{ product.category }}</span>
      </div>
    </div>

    <div v-else-if="showDropdown && query.length >= 2 && results.length === 0" class="search-dropdown">
      <div class="search-empty">Nenhum produto encontrado 😔</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  props: {
    products: { type: Array, default: () => [] }
  },
  emits: ['select-product'],
  data() {
    return {
      query: '',
      showDropdown: false
    }
  },
  computed: {
    results() {
      if (this.query.length < 2) return []
      const q = this.query.toLowerCase()
      return this.products.filter(p =>
        p.name.toLowerCase().includes(q) ||
        (p.description && p.description.toLowerCase().includes(q))
      ).slice(0, 6)
    }
  },
  methods: {
    onInput() {
      this.showDropdown = this.query.length >= 2
    },
    selectProduct(product) {
      this.$emit('select-product', product)
      this.close()
    },
    close() {
      this.showDropdown = false
    },
    clear() {
      this.query = ''
      this.showDropdown = false
      this.$refs.inputRef?.focus()
    }
  },
  mounted() {
    document.addEventListener('click', this.handleOutsideClick)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleOutsideClick)
  },
  methods: {
    onInput() {
      this.showDropdown = this.query.length >= 2
    },
    selectProduct(product) {
      this.$emit('select-product', product)
      this.close()
    },
    close() {
      this.showDropdown = false
    },
    clear() {
      this.query = ''
      this.showDropdown = false
      this.$refs.inputRef?.focus()
    },
    handleOutsideClick(e) {
      if (this.$refs.searchRef && !this.$refs.searchRef.contains(e.target)) {
        this.showDropdown = false
      }
    }
  }
}
</script>

<style scoped>
.search-bar {
  position: relative;
  width: 240px;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #fff5fb;
  border: 2px solid #fce4f0;
  border-radius: 50px;
  padding: 6px 14px;
  gap: 8px;
  transition: border-color 0.2s;
}

.search-input-wrap:focus-within {
  border-color: #e91e7b;
  background: white;
}

.search-icon {
  font-size: 0.9rem;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.9rem;
  outline: none;
  color: #333;
  font-family: inherit;
  min-width: 0;
}

.search-input::placeholder {
  color: #bbb;
}

.search-clear {
  background: none;
  border: none;
  cursor: pointer;
  color: #aaa;
  font-size: 0.8rem;
  padding: 0;
  line-height: 1;
  flex-shrink: 0;
}

.search-clear:hover { color: #e91e7b; }

.search-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  z-index: 200;
  overflow: hidden;
  min-width: 280px;
}

.search-result {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #fce4f0;
}

.search-result:last-child { border-bottom: none; }
.search-result:hover { background: #fff5fb; }

.result-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-price {
  font-size: 0.8rem;
  color: #e91e7b;
  font-weight: 700;
}

.result-cat {
  font-size: 0.75rem;
  background: #fce4f0;
  color: #c2185b;
  padding: 2px 8px;
  border-radius: 20px;
  flex-shrink: 0;
}

.search-empty {
  padding: 16px;
  text-align: center;
  color: #aaa;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .search-bar { width: 100%; }
}
</style>
