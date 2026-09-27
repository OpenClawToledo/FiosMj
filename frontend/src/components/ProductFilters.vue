<template>
  <div class="product-filters">
    <div class="filter-categories">
      <button
        v-for="cat in categories"
        :key="cat"
        class="cat-chip"
        :class="{ active: selectedCategory === cat }"
        @click="selectedCategory = cat"
      >
        {{ cat }}
      </button>
    </div>

    <div class="filter-sort">
      <label class="sort-label">Ordenar:</label>
      <select v-model="sortBy" class="sort-select">
        <option value="relevance">Relevância</option>
        <option value="price-asc">Menor preço</option>
        <option value="price-desc">Maior preço</option>
      </select>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductFilters',
  props: {
    products: { type: Array, default: () => [] }
  },
  emits: ['update:filtered'],
  data() {
    return {
      selectedCategory: 'Todos',
      sortBy: 'relevance',
      categories: ['Todos', 'Roupas', 'Amigurumi', 'Acessórios']
    }
  },
  computed: {
    filteredProducts() {
      let list = [...this.products]

      if (this.selectedCategory !== 'Todos') {
        list = list.filter(p => p.category === this.selectedCategory)
      }

      if (this.sortBy === 'price-asc') {
        list.sort((a, b) => a.price - b.price)
      } else if (this.sortBy === 'price-desc') {
        list.sort((a, b) => b.price - a.price)
      }

      return list
    }
  },
  watch: {
    filteredProducts: {
      immediate: true,
      handler(val) {
        this.$emit('update:filtered', val)
      }
    },
    products: {
      handler() {
        this.$emit('update:filtered', this.filteredProducts)
      }
    }
  }
}
</script>

<style scoped>
.product-filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 32px;
  padding: 16px 20px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(233,30,123,0.08);
}

.filter-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cat-chip {
  padding: 7px 18px;
  border: 2px solid #fce4f0;
  background: white;
  color: #c2185b;
  border-radius: 50px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.cat-chip:hover {
  background: #fce4f0;
}

.cat-chip.active {
  background: #e91e7b;
  border-color: #e91e7b;
  color: white;
}

.filter-sort {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 0.88rem;
  color: #888;
  font-weight: 600;
  white-space: nowrap;
}

.sort-select {
  padding: 7px 12px;
  border: 2px solid #fce4f0;
  border-radius: 8px;
  font-size: 0.88rem;
  color: #333;
  font-family: inherit;
  outline: none;
  cursor: pointer;
  background: white;
}

.sort-select:focus {
  border-color: #e91e7b;
}

@media (max-width: 600px) {
  .product-filters {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
