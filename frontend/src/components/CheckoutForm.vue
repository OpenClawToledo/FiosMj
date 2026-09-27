<template>
  <teleport to="body">
    <div class="checkout-overlay" @click.self="$emit('close')">
      <div class="checkout-modal">
        <!-- Header -->
        <div class="modal-header">
          <h2>🛒 Finalizar Pedido</h2>
          <button class="btn-close" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSubmit">

            <!-- Dados pessoais -->
            <section class="form-section">
              <h3 class="section-title">👤 Seus Dados</h3>

              <div class="form-group">
                <label>Nome completo *</label>
                <input v-model="form.name" type="text" placeholder="Seu nome completo" required />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Telefone (WhatsApp) *</label>
                  <input v-model="form.phone" type="tel" placeholder="(00) 00000-0000" required />
                </div>
                <div class="form-group">
                  <label>E-mail *</label>
                  <input v-model="form.email" type="email" placeholder="seu@email.com" required />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>CPF <span class="label-hint">(obrigatório para boleto)</span></label>
                  <input
                    v-model="form.cpf"
                    type="text"
                    placeholder="000.000.000-00"
                    maxlength="14"
                    @input="formatCpf"
                  />
                </div>
              </div>
            </section>

            <!-- Endereço -->
            <section class="form-section">
              <h3 class="section-title">📍 Endereço de Entrega</h3>

              <div class="cep-row">
                <div class="form-group cep-group">
                  <label>CEP *</label>
                  <div class="cep-input-wrap">
                    <input
                      v-model="form.cep"
                      type="text"
                      placeholder="00000-000"
                      maxlength="9"
                      @blur="fetchCep"
                      @input="formatCep"
                      required
                    />
                    <button type="button" class="btn-cep" @click="fetchCep" :disabled="cepLoading">
                      <span v-if="cepLoading" class="spinner">⟳</span>
                      <span v-else>Buscar</span>
                    </button>
                  </div>
                  <p v-if="cepError" class="field-error">{{ cepError }}</p>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group street-group">
                  <label>Rua *</label>
                  <input v-model="form.street" type="text" placeholder="Nome da rua" required />
                </div>
                <div class="form-group number-group">
                  <label>Número *</label>
                  <input v-model="form.number" type="text" placeholder="Nº" required />
                </div>
              </div>

              <div class="form-group">
                <label>Complemento</label>
                <input v-model="form.complement" type="text" placeholder="Apto, bloco, referência (opcional)" />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Bairro *</label>
                  <input v-model="form.neighborhood" type="text" placeholder="Bairro" required />
                </div>
                <div class="form-group">
                  <label>Cidade *</label>
                  <input v-model="form.city" type="text" placeholder="Cidade" required />
                </div>
                <div class="form-group estado-group">
                  <label>Estado *</label>
                  <select v-model="form.state" required>
                    <option value="" disabled>UF</option>
                    <option v-for="uf in ufs" :key="uf" :value="uf">{{ uf }}</option>
                  </select>
                </div>
              </div>
            </section>

            <!-- Resumo do pedido -->
            <section class="form-section">
              <h3 class="section-title">📦 Resumo do Pedido</h3>
              <div class="order-items">
                <div v-for="item in items" :key="item.key" class="order-item">
                  <span class="order-item-name">
                    {{ item.product.name }}
                    <span v-if="item.selectedSize" class="order-item-size">({{ item.selectedSize.size }})</span>
                    × {{ item.quantity }}
                  </span>
                  <span class="order-item-price">R$ {{ (item.price * item.quantity).toFixed(2).replace('.', ',') }}</span>
                </div>
              </div>
              <div class="order-total-row">
                <span>Total:</span>
                <span class="order-total">R$ {{ total.toFixed(2).replace('.', ',') }}</span>
              </div>
              <p class="frete-note">🚚 Frete: a calcular — informaremos pelo WhatsApp após o pedido.</p>
            </section>

            <!-- Erro de submissão -->
            <p v-if="submitError" class="submit-error">{{ submitError }}</p>

            <!-- Submit -->
            <button type="submit" class="btn-payment" :disabled="submitting">
              <span v-if="submitting" class="spinner">⟳</span>
              <span v-else>Ir para Pagamento →</span>
            </button>

          </form>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script>
import { useCart } from '../store/cart.js'
import { useAuth } from '../store/auth.js'

export default {
  name: 'CheckoutForm',
  emits: ['close'],
  setup() {
    const { items, total, clearCart } = useCart()
    const { customer, isLoggedIn } = useAuth()
    return { items, total, clearCart, authCustomer: customer, isLoggedIn }
  },
  data() {
    return {
      form: {
        name: '',
        phone: '',
        email: '',
        cpf: '',
        cep: '',
        street: '',
        number: '',
        complement: '',
        neighborhood: '',
        city: '',
        state: ''
      },
      cepLoading: false,
      cepError: '',
      submitting: false,
      submitError: '',
      ufs: [
        'AC','AL','AM','AP','BA','CE','DF','ES','GO',
        'MA','MG','MS','MT','PA','PB','PE','PI','PR',
        'RJ','RN','RO','RR','RS','SC','SE','SP','TO'
      ]
    }
  },
  mounted() {
    // Prefill from logged-in customer
    if (this.isLoggedIn && this.authCustomer.customer) {
      const c = this.authCustomer.customer
      if (c.name) this.form.name = c.name
      if (c.email) this.form.email = c.email
      if (c.phone) this.form.phone = c.phone
    }
  },
  methods: {
    formatCpf() {
      let v = this.form.cpf.replace(/\D/g, '')
      if (v.length > 9) v = v.slice(0, 3) + '.' + v.slice(3, 6) + '.' + v.slice(6, 9) + '-' + v.slice(9, 11)
      else if (v.length > 6) v = v.slice(0, 3) + '.' + v.slice(3, 6) + '.' + v.slice(6)
      else if (v.length > 3) v = v.slice(0, 3) + '.' + v.slice(3)
      this.form.cpf = v
    },

    formatCep() {
      let v = this.form.cep.replace(/\D/g, '')
      if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5, 8)
      this.form.cep = v
    },

    async fetchCep() {
      const raw = this.form.cep.replace(/\D/g, '')
      if (raw.length !== 8) return
      this.cepLoading = true
      this.cepError = ''
      try {
        const res = await fetch(`https://viacep.com.br/ws/${raw}/json/`)
        const data = await res.json()
        if (data.erro) {
          this.cepError = 'CEP não encontrado. Verifique e tente novamente.'
          return
        }
        this.form.street = data.logradouro || ''
        this.form.neighborhood = data.bairro || ''
        this.form.city = data.localidade || ''
        this.form.state = data.uf || ''
      } catch {
        this.cepError = 'Erro ao buscar CEP. Verifique sua conexão.'
      } finally {
        this.cepLoading = false
      }
    },

    async handleSubmit() {
      if (this.items.length === 0) {
        this.submitError = 'Seu carrinho está vazio.'
        return
      }
      this.submitting = true
      this.submitError = ''
      try {
        const body = {
          items: this.items.map(i => ({
            productId: i.product.id,
            productName: i.product.name + (i.selectedSize ? ` (${i.selectedSize.size})` : ''),
            price: i.price,
            quantity: i.quantity
          })),
          payer: {
            name: this.form.name,
            email: this.form.email || 'cliente@fiosmj.com',
            phone: this.form.phone,
            cpf: this.form.cpf ? this.form.cpf.replace(/\D/g, '') : null
          },
          address: {
            cep: this.form.cep,
            street: this.form.street,
            number: this.form.number,
            complement: this.form.complement,
            neighborhood: this.form.neighborhood,
            city: this.form.city,
            state: this.form.state
          }
        }

        const token = localStorage.getItem('fiosmj_token')
        const authHeaders = { 'Content-Type': 'application/json' }
        if (token) authHeaders['Authorization'] = `Bearer ${token}`

        // 1. Se logado: criar pedido ANTES da preference para usar o ID como external_reference
        let orderId = null
        if (token) {
          try {
            const orderPayload = {
              name: this.form.name, email: this.form.email, phone: this.form.phone,
              cep: this.form.cep, street: this.form.street, number: this.form.number,
              complement: this.form.complement, neighborhood: this.form.neighborhood,
              city: this.form.city, state: this.form.state,
              totalAmount: this.total,
              items: this.items.map(i => ({
                productName: i.product.name + (i.selectedSize ? ` (${i.selectedSize.size})` : ''),
                price: i.price, quantity: i.quantity,
                selectedSize: i.selectedSize?.size || null
              }))
            }
            const orderRes = await fetch('/api/orders', {
              method: 'POST',
              headers: authHeaders,
              body: JSON.stringify(orderPayload)
            })
            if (orderRes.ok) {
              const orderData = await orderRes.json()
              orderId = orderData.id
            }
          } catch {}
        }

        // 2. Criar preference no Mercado Pago (com external_reference = order ID)
        if (orderId) body.externalReference = `order-${orderId}`

        const res = await fetch('/api/checkout/preference', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        })

        if (!res.ok) {
          const err = await res.json().catch(() => ({}))
          throw new Error(err.error || `Erro ${res.status}`)
        }

        const data = await res.json()
        if (data.init_point) {
          // 3. Atualizar pedido com preferenceId
          if (token && orderId && data.preference_id) {
            fetch(`/api/orders/${orderId}/preference`, {
              method: 'PATCH',
              headers: authHeaders,
              body: JSON.stringify({ preferenceId: data.preference_id, initPoint: data.init_point })
            }).catch(() => {})
          }
          this.clearCart()
          window.location.href = data.init_point
        } else {
          throw new Error(data.error || 'Resposta inválida do servidor')
        }
      } catch (err) {
        this.submitError = 'Erro ao processar pagamento: ' + err.message
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.checkout-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1100;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow-y: auto;
  padding: 20px;
}

.checkout-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 640px;
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

.btn-close:hover {
  background: #f0f0f0;
}

.modal-body {
  padding: 28px;
}

.form-section {
  margin-bottom: 28px;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #e91e7b;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #fce4f0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #555;
}

.form-group input,
.form-group select {
  padding: 10px 14px;
  border: 2px solid #f0d0e4;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #333;
  background: white;
  transition: border-color 0.2s;
  font-family: inherit;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #e91e7b;
}

.form-row {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.form-row .form-group {
  flex: 1;
  min-width: 120px;
}

.street-group { flex: 3 !important; }
.number-group { flex: 1 !important; min-width: 80px !important; }
.estado-group { flex: 0 0 80px !important; min-width: 80px !important; }

/* CEP */
.cep-row { display: flex; }
.cep-group { max-width: 240px; }

.cep-input-wrap {
  display: flex;
  gap: 8px;
}

.cep-input-wrap input {
  flex: 1;
}

.btn-cep {
  padding: 10px 14px;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.2s;
  font-family: inherit;
}

.btn-cep:hover:not(:disabled) { opacity: 0.85; }
.btn-cep:disabled { opacity: 0.6; cursor: not-allowed; }

.label-hint {
  font-weight: 400;
  color: #aaa;
  font-size: 0.78rem;
}

.field-error {
  font-size: 0.8rem;
  color: #e53935;
  margin: 0;
}

/* Order summary */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fffafd;
  border: 1px solid #f5e0ec;
  border-radius: 8px;
  font-size: 0.9rem;
}

.order-item-name { color: #333; flex: 1; }
.order-item-size { color: #e91e7b; font-size: 0.8rem; }
.order-item-price { font-weight: 700; color: #e91e7b; }

.order-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 2px solid #fce4f0;
  font-weight: 600;
  font-size: 1.05rem;
}

.order-total {
  font-size: 1.4rem;
  font-weight: 700;
  color: #e91e7b;
  font-family: 'Playfair Display', serif;
}

.frete-note {
  font-size: 0.82rem;
  color: #999;
  margin: 8px 0 0;
}

/* Errors / Submit */
.submit-error {
  background: #fdecea;
  color: #c62828;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  margin-bottom: 16px;
}

.btn-payment {
  width: 100%;
  padding: 16px;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1.05rem;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
  font-family: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-payment:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-payment:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .modal-body { padding: 20px; }
  .form-row { flex-direction: column; }
  .street-group, .number-group, .estado-group { flex: unset !important; min-width: unset !important; }
}
</style>
