<template>
  <section class="newsletter-section">
    <div class="newsletter-inner">
      <div class="newsletter-icon">💌</div>
      <h2 class="newsletter-title">Recebe novidades da Fios MJ</h2>
      <p class="newsletter-sub">Lançamentos, promoções e tutoriais exclusivos direto no teu e-mail 🧶</p>

      <form v-if="!success" class="newsletter-form" @submit.prevent="subscribe">
        <div class="form-row">
          <input
            v-model="name"
            type="text"
            placeholder="Teu nome (opcional)"
            class="input-name"
          />
          <input
            v-model="email"
            type="email"
            placeholder="Teu melhor e-mail *"
            required
            class="input-email"
          />
          <button type="submit" class="btn-subscribe" :disabled="loading">
            <span v-if="loading">⟳</span>
            <span v-else>Quero receber! →</span>
          </button>
        </div>
        <p v-if="error" class="nl-error">{{ error }}</p>
        <p class="nl-privacy">Sem spam. Podes cancelar quando quiseres. 🔒</p>
      </form>

      <div v-else class="success-msg">
        <div class="success-icon">🎉</div>
        <p>{{ successMessage }}</p>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'NewsletterSection',
  data() {
    return {
      name: '',
      email: '',
      loading: false,
      success: false,
      successMessage: '',
      error: ''
    }
  },
  methods: {
    async subscribe() {
      this.loading = true
      this.error = ''
      try {
        const res = await fetch('/api/newsletter/subscribe', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: this.email, name: this.name || null })
        })
        const data = await res.json()
        if (res.ok && data.success) {
          this.success = true
          this.successMessage = data.message || 'Inscrita com sucesso! 💕'
        } else {
          this.error = data.error || 'Erro ao subscrever. Tenta novamente.'
        }
      } catch {
        this.error = 'Erro de conexão. Tenta novamente.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.newsletter-section {
  background: linear-gradient(135deg, #c2185b 0%, #e91e7b 50%, #f06292 100%);
  padding: 60px 20px;
  text-align: center;
}

.newsletter-inner {
  max-width: 600px;
  margin: 0 auto;
}

.newsletter-icon { font-size: 2.5rem; margin-bottom: 12px; }

.newsletter-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  color: white;
  margin-bottom: 10px;
}

.newsletter-sub {
  color: rgba(255,255,255,0.85);
  font-size: 0.95rem;
  margin-bottom: 28px;
}

.newsletter-form {}

.form-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 10px;
}

.input-name, .input-email {
  padding: 14px 18px;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  flex: 1;
  min-width: 160px;
  max-width: 220px;
  background: rgba(255,255,255,0.95);
  color: #333;
}

.input-email { max-width: 260px; }

.btn-subscribe {
  padding: 14px 24px;
  background: white;
  color: #e91e7b;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.btn-subscribe:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}
.btn-subscribe:disabled { opacity: 0.7; cursor: not-allowed; }

.nl-error {
  color: #ffd0d0;
  font-size: 0.85rem;
  margin: 6px 0 0;
}

.nl-privacy {
  color: rgba(255,255,255,0.65);
  font-size: 0.78rem;
  margin-top: 8px;
}

.success-msg {
  padding: 24px;
  background: rgba(255,255,255,0.15);
  border-radius: 16px;
}
.success-icon { font-size: 2.5rem; margin-bottom: 10px; }
.success-msg p {
  color: white;
  font-size: 1.05rem;
  font-weight: 600;
  margin: 0;
}

@media (max-width: 480px) {
  .form-row { flex-direction: column; align-items: stretch; }
  .input-name, .input-email { max-width: 100%; }
  .newsletter-title { font-size: 1.4rem; }
}
</style>
