<template>
  <header class="header">
    <div class="container header-inner">
      <a href="#" class="logo">
        <img :src="logoSrc" alt="Fios MJ — Artes em Crochê" class="logo-img" />
      </a>
      <nav class="nav" :class="{ open: menuOpen }">
        <a href="#produtos" @click="menuOpen = false">Produtos</a>
        <a href="#como-pedir" @click="menuOpen = false">Como Pedir</a>
        <a href="#sobre" @click="menuOpen = false">Sobre</a>
        <a href="https://instagram.com/fiosmjcroche" target="_blank" rel="noopener" class="nav-instagram">
          📸 Instagram
        </a>
      </nav>

      <!-- Search Bar -->
      <SearchBar :products="products" @select-product="$emit('open-product', $event)" class="header-search" />

      <!-- Account button -->
      <div class="account-wrap" v-if="isLoggedIn" ref="accountRef">
        <button class="account-btn" @click="dropdownOpen = !dropdownOpen">
          👤 Olá, {{ firstName }}
          <span class="chevron">▾</span>
        </button>
        <div class="dropdown" v-if="dropdownOpen" @click.stop>
          <button class="dropdown-item" @click="openOrders">📦 Meus Pedidos</button>
          <button class="dropdown-item" @click="handleLogout">🚪 Sair</button>
        </div>
      </div>
      <button v-else class="account-btn" @click="$emit('open-auth')">
        👤 Entrar
      </button>

      <!-- Cart button -->
      <button class="cart-btn" @click="$emit('open-cart')" aria-label="Abrir carrinho">
        🛒
        <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
      </button>

      <button class="menu-toggle" @click="menuOpen = !menuOpen" aria-label="Menu">
        <span></span><span></span><span></span>
      </button>
    </div>
  </header>
</template>

<script>
import { useCart } from '../store/cart.js'
import { useAuth } from '../store/auth.js'
import { computed } from 'vue'

import SearchBar from './SearchBar.vue'

export default {
  name: 'AppHeader',
  components: { SearchBar },
  props: {
    products: { type: Array, default: () => [] }
  },
  emits: ['open-cart', 'open-auth', 'open-orders', 'open-product'],
  setup() {
    const { count } = useCart()
    const { customer, isLoggedIn, logout } = useAuth()
    const firstName = computed(() => customer.customer?.name?.split(' ')[0] || '')
    return { cartCount: count, customer, isLoggedIn, logout, firstName }
  },
  data() {
    return {
      menuOpen: false,
      dropdownOpen: false,
      logoSrc: '/img/logo-fiosmj.jpg'
    }
  },
  mounted() {
    document.addEventListener('click', this.closeDropdown)
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeDropdown)
  },
  methods: {
    closeDropdown(e) {
      if (this.$refs.accountRef && !this.$refs.accountRef.contains(e.target)) {
        this.dropdownOpen = false
      }
    },
    handleLogout() {
      this.logout()
      this.dropdownOpen = false
    },
    openOrders() {
      this.dropdownOpen = false
      this.$emit('open-orders')
    }
  }
}
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 200;
  background: #FDF8F2;
  box-shadow: 0 2px 12px rgba(196, 115, 106, 0.15);
  border-bottom: 1px solid #E8D5BE;
  /* Necessário para o dropdown absolute funcionar */
  isolation: isolate;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-img {
  height: 52px;
  width: 52px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(196,115,106,0.25);
  transition: transform 0.3s ease;
}

.logo-img:hover {
  transform: scale(1.06);
}

.nav {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav a {
  font-weight: 500;
  color: var(--text-dark);
  transition: color 0.3s;
  font-size: 0.95rem;
}

.nav a:hover {
  color: var(--pink-primary);
}

.nav-instagram {
  color: var(--pink-primary) !important;
  font-weight: 600 !important;
}

/* Account */
.account-wrap {
  position: relative;
}

.account-btn {
  background: none;
  border: 2px solid var(--pink-primary);
  border-radius: 50px;
  padding: 6px 14px;
  font-size: 0.9rem;
  cursor: pointer;
  color: var(--pink-primary);
  font-weight: 600;
  transition: background 0.2s, color 0.2s;
  font-family: inherit;
  display: flex;
  align-items: center;
  gap: 4px;
}

.account-btn:hover {
  background: var(--pink-primary);
  color: white;
}

.chevron {
  font-size: 0.75rem;
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  min-width: 160px;
  overflow: hidden;
  z-index: 200;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
  transition: background 0.2s;
  font-family: inherit;
}

.dropdown-item:hover {
  background: #fff5fb;
  color: #e91e7b;
}

/* Cart button */
.cart-btn {
  position: relative;
  background: none;
  border: 2px solid var(--pink-primary);
  border-radius: 50px;
  padding: 6px 14px;
  font-size: 1.1rem;
  cursor: pointer;
  color: var(--pink-primary);
  transition: background 0.2s, color 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1;
}

.cart-btn:hover {
  background: var(--pink-primary);
  color: white;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #e91e7b;
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
  border: 2px solid white;
  line-height: 1;
}

.cart-btn:hover .cart-badge {
  background: white;
  color: #e91e7b;
}

.menu-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
}

.menu-toggle span {
  display: block;
  width: 25px;
  height: 3px;
  background: var(--pink-primary);
  border-radius: 3px;
  transition: var(--transition);
}

.header-search {
  flex: 0 1 240px;
}

@media (max-width: 768px) {
  /* Esconder barra de pesquisa */
  .header-search { display: none; }

  /* Mostrar hamburger */
  .menu-toggle { display: flex; }

  /* Nav: dropdown em cascata — absolute ao header, não fixed */
  .nav {
    position: absolute;
    top: 70px;
    left: 0;
    right: 0;
    background: #FDF8F2;
    border-top: 1px solid #E8D5BE;
    border-bottom: 2px solid #C4736A;
    flex-direction: column;
    align-items: flex-start;
    padding: 0;
    gap: 0;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.35s ease;
    z-index: 199;
    /* Sem position:fixed — não interfere com scroll */
  }

  .nav.open {
    max-height: 400px;
    padding: 4px 0;
  }

  .nav a {
    display: block;
    width: 100%;
    padding: 14px 24px;
    font-size: 1rem;
    border-bottom: 1px solid rgba(232,213,190,0.4);
  }
  .nav a:last-child { border-bottom: none; }
  .nav a:active { background: rgba(196,115,106,0.08); }

  /* "Olá, X" — só mostrar ícone */
  .account-btn span:not(.chevron) { display: none; }
}
</style>
