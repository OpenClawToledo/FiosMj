<template>
  <teleport to="body">
    <div class="orders-overlay" @click.self="$emit('close')">
      <div class="orders-modal">
        <div class="modal-header">
          <h2>📦 Meus Pedidos</h2>
          <button class="btn-close" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <div v-if="loading" class="loading">Carregando...</div>

          <div v-else-if="orders.length === 0" class="empty">
            <p>Você ainda não fez nenhum pedido.</p>
          </div>

          <div v-else class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-card">
              <div class="order-header">
                <div class="order-meta">
                  <span class="order-id">#{{ order.id }}</span>
                  <span :class="['status-badge', statusClass(order.status)]">{{ statusLabel(order.status) }}</span>
                </div>
                <div class="order-date">{{ formatDate(order.createdAt) }}</div>
              </div>

              <div class="order-items">
                <div v-for="item in order.items" :key="item.id" class="order-item">
                  <span>{{ item.productName }}<span v-if="item.selectedSize"> ({{ item.selectedSize }})</span> × {{ item.quantity }}</span>
                  <span class="item-price">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                </div>
              </div>

              <div class="order-total">
                <span>Total:</span>
                <strong>R$ {{ (order.totalAmount || 0).toFixed(2).replace('.', ',') }}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script>
export default {
  name: 'OrderHistory',
  emits: ['close'],
  data() {
    return {
      orders: [],
      loading: true
    }
  },
  async mounted() {
    await this.loadOrders()
  },
  methods: {
    async loadOrders() {
      const token = localStorage.getItem('fiosmj_token')
      if (!token) { this.loading = false; return }
      try {
        const res = await fetch('/api/orders', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (res.ok) this.orders = await res.json()
      } catch {}
      finally { this.loading = false }
    },
    statusLabel(status) {
      return { PENDING: 'Pendente', CONFIRMED: 'Confirmado', CANCELLED: 'Cancelado' }[status] || status
    },
    statusClass(status) {
      return { PENDING: 'status-pending', CONFIRMED: 'status-confirmed', CANCELLED: 'status-cancelled' }[status] || ''
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.orders-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1200;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow-y: auto;
  padding: 20px;
}

.orders-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  margin: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 2px solid #fce4f0;
  background: #fff5fb;
  border-radius: 16px 16px 0 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #e91e7b;
  font-weight: 700;
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
}

.btn-close:hover { background: #f0f0f0; }

.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.loading, .empty {
  text-align: center;
  color: #888;
  padding: 40px 0;
  font-size: 1rem;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #f5e0ec;
  border-radius: 12px;
  padding: 16px;
  background: #fffafd;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-id {
  font-weight: 700;
  color: #333;
}

.status-badge {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-pending { background: #fff8e1; color: #f57f17; }
.status-confirmed { background: #e8f5e9; color: #2e7d32; }
.status-cancelled { background: #fdecea; color: #c62828; }

.order-date {
  font-size: 0.82rem;
  color: #888;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: #555;
}

.item-price {
  font-weight: 600;
  color: #e91e7b;
}

.order-total {
  display: flex;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f0d0e4;
  font-size: 0.95rem;
}

.order-total strong {
  color: #e91e7b;
  font-size: 1.1rem;
}
</style>
