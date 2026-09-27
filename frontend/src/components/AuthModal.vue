<template>
  <teleport to="body">
    <div class="auth-overlay" @click.self="$emit('close')">
      <div class="auth-modal">
        <div class="modal-header">
          <div class="tabs">
            <button :class="['tab', { active: tab === 'login' }]" @click="tab = 'login'">Entrar</button>
            <button :class="['tab', { active: tab === 'register' }]" @click="tab = 'register'">Criar conta</button>
          </div>
          <button class="btn-close" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <!-- Login -->
          <form v-if="tab === 'login'" @submit.prevent="handleLogin">
            <div class="form-group">
              <label>E-mail</label>
              <input v-model="loginForm.email" type="email" placeholder="seu@email.com" required />
            </div>
            <div class="form-group">
              <label>Senha</label>
              <input v-model="loginForm.password" type="password" placeholder="Sua senha" required />
            </div>
            <p v-if="error" class="error-msg">{{ error }}</p>
            <button type="submit" class="btn-primary" :disabled="loading">
              <span v-if="loading" class="spinner">⟳</span>
              <span v-else>Entrar</span>
            </button>
            <p class="switch-tab">Não tem conta? <a href="#" @click.prevent="tab = 'register'">Criar conta</a></p>
          </form>

          <!-- Register -->
          <form v-else @submit.prevent="handleRegister">
            <div class="form-group">
              <label>Nome completo</label>
              <input v-model="registerForm.name" type="text" placeholder="Seu nome" required />
            </div>
            <div class="form-group">
              <label>E-mail</label>
              <input v-model="registerForm.email" type="email" placeholder="seu@email.com" required />
            </div>
            <div class="form-group">
              <label>Telefone (WhatsApp)</label>
              <input v-model="registerForm.phone" type="tel" placeholder="(00) 00000-0000" />
            </div>
            <div class="form-group">
              <label>Senha</label>
              <input v-model="registerForm.password" type="password" placeholder="Mínimo 6 caracteres" minlength="6" required />
            </div>
            <p v-if="error" class="error-msg">{{ error }}</p>
            <button type="submit" class="btn-primary" :disabled="loading">
              <span v-if="loading" class="spinner">⟳</span>
              <span v-else>Criar conta</span>
            </button>
            <p class="switch-tab">Já tem conta? <a href="#" @click.prevent="tab = 'login'">Entrar</a></p>
          </form>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script>
import { useAuth } from '../store/auth.js'
import { useCart } from '../store/cart.js'

export default {
  name: 'AuthModal',
  emits: ['close', 'logged-in'],
  setup() {
    const { login, register } = useAuth()
    const { loadFromServer } = useCart()
    return { login, register, loadFromServer }
  },
  data() {
    return {
      tab: 'login',
      loading: false,
      error: '',
      loginForm: { email: '', password: '' },
      registerForm: { name: '', email: '', phone: '', password: '' }
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = ''
      try {
        await this.login(this.loginForm.email, this.loginForm.password)
        await this.loadFromServer()
        this.$emit('logged-in')
        this.$emit('close')
      } catch (e) {
        this.error = e.message
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      this.loading = true
      this.error = ''
      try {
        await this.register(
          this.registerForm.name,
          this.registerForm.email,
          this.registerForm.phone,
          this.registerForm.password
        )
        await this.loadFromServer()
        this.$emit('logged-in')
        this.$emit('close')
      } catch (e) {
        this.error = e.message
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab {
  background: none;
  border: none;
  padding: 8px 16px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #999;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  font-family: inherit;
}

.tab.active {
  color: #e91e7b;
  border-bottom-color: #e91e7b;
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

.form-group input {
  padding: 10px 14px;
  border: 2px solid #f0d0e4;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-group input:focus {
  border-color: #e91e7b;
}

.error-msg {
  background: #fdecea;
  color: #c62828;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) { opacity: 0.9; }
.btn-primary:disabled { opacity: 0.7; cursor: not-allowed; }

.switch-tab {
  text-align: center;
  font-size: 0.88rem;
  color: #888;
  margin-top: 12px;
}

.switch-tab a {
  color: #e91e7b;
  font-weight: 600;
  text-decoration: none;
}

.spinner {
  display: inline-block;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
