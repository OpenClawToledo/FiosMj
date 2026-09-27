<template>
  <teleport to="body">
    <!-- Overlay -->
    <transition name="fade">
      <div v-if="open" class="cart-overlay" @click="$emit('close')"></div>
    </transition>

    <!-- Drawer -->
    <transition name="slide">
      <div v-if="open" class="cart-drawer">
        <!-- Header -->
        <div class="drawer-header">
          <div class="drawer-tabs">
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'cart' }"
              @click="activeTab = 'cart'"
            >🛒 Carrinho <span class="cart-count-badge" v-if="count > 0">{{ count }}</span></button>
            <button
              class="tab-btn"
              :class="{ active: activeTab === 'orders' }"
              @click="switchToOrders"
            >📦 Meus Pedidos</button>
          </div>
          <button class="btn-close" @click="$emit('close')" aria-label="Fechar">✕</button>
        </div>

        <!-- ─── CART TAB ─── -->
        <template v-if="activeTab === 'cart'">
          <!-- Empty state -->
          <div v-if="items.length === 0" class="cart-empty">
            <span class="empty-icon">🛍️</span>
            <p>Seu carrinho está vazio.</p>
            <p class="empty-sub">Adicione produtos lindos da Maju! 🧶</p>
          </div>

          <!-- Items list -->
          <div v-else class="cart-items">
            <div v-for="item in items" :key="item.key" class="cart-item">
              <img
                :src="item.product.imageUrl"
                :alt="item.product.name"
                class="item-image"
              />
              <div class="item-details">
                <p class="item-name">{{ item.product.name }}</p>
                <p v-if="item.selectedSize" class="item-size">Tamanho: {{ item.selectedSize.size }}</p>
                <p class="item-price">R$ {{ item.price.toFixed(2).replace('.', ',') }} un.</p>
              </div>
              <div class="item-qty">
                <button class="qty-btn" @click="updateQty(item.key, -1)">−</button>
                <span class="qty-value">{{ item.quantity }}</span>
                <button class="qty-btn" @click="updateQty(item.key, 1)">+</button>
              </div>
              <button class="btn-remove" @click="removeItem(item.key)" aria-label="Remover">🗑</button>
            </div>
          </div>

          <!-- Footer -->
          <div v-if="items.length > 0" class="drawer-footer">
            <div class="cart-total">
              <span>Total:</span>
              <span class="total-value">R$ {{ total.toFixed(2).replace('.', ',') }}</span>
            </div>
            <button class="btn-checkout" @click="$emit('checkout')">
              Finalizar Compra →
            </button>
          </div>
        </template>

        <!-- ─── ORDERS TAB ─── -->
        <template v-else>
          <!-- Not logged in -->
          <div v-if="!isLoggedIn" class="orders-login">
            <span class="empty-icon">👤</span>
            <p>Faça login para ver seus pedidos</p>
            <button class="btn-login" @click="$emit('open-auth'); $emit('close')">
              Entrar / Criar Conta
            </button>
          </div>

          <!-- Loading -->
          <div v-else-if="loadingOrders" class="orders-loading">
            <span>⏳</span>
            <p>Carregando seus pedidos...</p>
          </div>

          <!-- Empty orders -->
          <div v-else-if="orders.length === 0" class="cart-empty">
            <span class="empty-icon">📦</span>
            <p>Você ainda não tem pedidos.</p>
            <p class="empty-sub">Compre algo incrível! 🧶</p>
          </div>

          <!-- Orders list -->
          <div v-else class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">#{{ order.id }}</span>
                <span class="order-status" :class="statusClass(order.status)">
                  {{ statusLabel(order.status) }}
                </span>
                <span class="order-date">{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="order-summary">{{ order.itemsSummary || 'Pedido' }}</div>
              <div class="order-footer">
                <span class="order-total">R$ {{ order.totalAmount?.toFixed(2).replace('.', ',') }}</span>
                <button class="btn-repeat" @click="repeatOrder(order)">🔄 Repetir</button>
              </div>
            </div>
          </div>
        </template>

      </div>
    </transition>
  </teleport>
</template>

<script>
import { useCart } from '../store/cart.js'
import { useAuth } from '../store/auth.js'

export default {
  name: 'CartDrawer',
  props: {
    open: { type: Boolean, default: false }
  },
  emits: ['close', 'checkout', 'open-auth'],
  setup() {
    const { items, removeItem, updateQty, total, count, addItem } = useCart()
    const { isLoggedIn } = useAuth()
    return { items, removeItem, updateQty, total, count, addItem, isLoggedIn }
  },
  data() {
    return {
      activeTab: 'cart',
      orders: [],
      loadingOrders: false
    }
  },
  methods: {
    async switchToOrders() {
      this.activeTab = 'orders'
      if (this.isLoggedIn && this.orders.length === 0) {
        await this.fetchOrders()
      }
    },
    async fetchOrders() {
      this.loadingOrders = true
      try {
        const token = localStorage.getItem('fiosmj_token')
        const res = await fetch('/api/orders', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (res.ok) this.orders = await res.json()
      } catch {}
      this.loadingOrders = false
    },
    statusLabel(status) {
      const map = {
        PENDING: '🟡 Pendente',
        CONFIRMED: '🟢 Confirmado',
        CANCELLED: '🔴 Cancelado',
        SHIPPED: '📬 Enviado',
        DELIVERED: '✅ Entregue'
      }
      return map[status] || status
    },
    statusClass(status) {
      return {
        PENDING: 'status-pending',
        CONFIRMED: 'status-confirmed',
        CANCELLED: 'status-cancelled',
        SHIPPED: 'status-shipped',
        DELIVERED: 'status-delivered'
      }[status] || ''
    },
    formatDate(dt) {
      if (!dt) return ''
      return new Date(dt).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    },
    repeatOrder(order) {
      if (!order.items || !order.items.length) return
      for (const item of order.items) {
        const product = {
          id: item.productId,
          name: item.productName,
          price: item.price,
          imageUrl: item.imageUrl || '/img/caneta.jpg'
        }
        const selectedSize = item.selectedSize ? { size: item.selectedSize, price: item.price } : null
        this.addItem(product, item.quantity, selectedSize)
      }
      this.activeTab = 'cart'
    }
  }
}
</script>

<style scoped>
.cart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.cart-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 400px;
  max-width: 100vw;
  background: white;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 30px rgba(0, 0, 0, 0.15);
}

/* Header & Tabs */
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 2px solid #fce4f0;
  background: #fff5fb;
  gap: 10px;
}

.drawer-tabs {
  display: flex;
  gap: 6px;
  flex: 1;
}

.tab-btn {
  flex: 1;
  padding: 8px 12px;
  background: none;
  border: 2px solid #fce4f0;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
  color: #888;
  font-family: inherit;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.tab-btn.active {
  background: #e91e7b;
  border-color: #e91e7b;
  color: white;
}

.cart-count-badge {
  background: white;
  color: #e91e7b;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 0.72rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tab-btn.active .cart-count-badge {
  background: white;
  color: #e91e7b;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #999;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
  flex-shrink: 0;
}

.btn-close:hover {
  background: #f0f0f0;
  color: #555;
}

/* Empty state */
.cart-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #aaa;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
}

.empty-sub {
  font-size: 0.85rem;
  color: #ccc;
}

/* Items */
.cart-items {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #f5e0ec;
  border-radius: 12px;
  background: #fffafd;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.item-details {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-size {
  font-size: 0.78rem;
  color: #e91e7b;
  margin: 0 0 2px;
}

.item-price {
  font-size: 0.82rem;
  color: #888;
  margin: 0;
}

.item-qty {
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #e91e7b;
  background: white;
  color: #e91e7b;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s;
  flex-shrink: 0;
}

.qty-btn:hover {
  background: #e91e7b;
  color: white;
}

.qty-value {
  font-weight: 700;
  font-size: 1rem;
  min-width: 20px;
  text-align: center;
}

.btn-remove {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  opacity: 0.5;
  transition: opacity 0.2s;
  flex-shrink: 0;
  padding: 4px;
}

.btn-remove:hover {
  opacity: 1;
}

/* Footer */
.drawer-footer {
  border-top: 2px solid #fce4f0;
  padding: 20px 24px;
  background: #fff5fb;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.total-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #e91e7b;
  font-family: 'Playfair Display', serif;
}

.btn-checkout {
  width: 100%;
  padding: 14px;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
  font-family: inherit;
}

.btn-checkout:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* Orders tab */
.orders-login {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  text-align: center;
  color: #666;
}

.orders-login .empty-icon { font-size: 3rem; }

.btn-login {
  background: #e91e7b;
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  margin-top: 8px;
}

.orders-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #aaa;
  font-size: 2rem;
}

.orders-loading p { font-size: 0.95rem; }

.orders-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  border: 2px solid #fce4f0;
  border-radius: 12px;
  padding: 14px;
  background: #fffafd;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.order-id {
  font-weight: 700;
  font-size: 0.88rem;
  color: #888;
}

.order-status {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 20px;
}

.status-pending { background: #fff3e0; color: #e65100; }
.status-confirmed { background: #e8f5e9; color: #2e7d32; }
.status-cancelled { background: #ffebee; color: #c62828; }
.status-shipped { background: #e3f2fd; color: #1565c0; }
.status-delivered { background: #f1f8e9; color: #33691e; }

.order-date {
  font-size: 0.78rem;
  color: #aaa;
  margin-left: auto;
}

.order-summary {
  font-size: 0.88rem;
  color: #555;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-total {
  font-weight: 700;
  color: #e91e7b;
  font-size: 1rem;
}

.btn-repeat {
  background: none;
  border: 2px solid #e91e7b;
  color: #e91e7b;
  padding: 5px 12px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-repeat:hover {
  background: #e91e7b;
  color: white;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.slide-enter-active,
.slide-leave-active { transition: transform 0.3s ease; }
.slide-enter-from,
.slide-leave-to { transform: translateX(100%); }
</style>
