<template>
  <section class="testimonials-section">
    <div class="section-inner">
      <h2 class="section-title">💬 O que dizem nossas clientes</h2>
      <p class="section-sub">Feedbacks reais de quem recebeu um pedacinho do nosso coração 💕</p>

      <!-- Grid de depoimentos -->
      <div class="testimonials-grid" v-if="testimonials.length">
        <div
          v-for="t in testimonials"
          :key="t.id"
          class="testimonial-card"
          :class="{ 'has-image': t.imageUrl }"
        >
          <!-- Print do WhatsApp / imagem -->
          <div v-if="t.imageUrl" class="testimonial-img-wrap">
            <img :src="t.imageUrl" :alt="'Feedback de ' + t.customerName" />
          </div>

          <div class="testimonial-body">
            <div class="stars">{{ '⭐'.repeat(t.rating || 5) }}</div>
            <p class="testimonial-msg">"{{ t.message }}"</p>
            <div class="testimonial-meta">
              <span class="customer-name">{{ t.customerName }}</span>
              <span v-if="t.city" class="customer-city">📍 {{ t.city }}</span>
              <span v-if="t.occasion" class="occasion-badge">🎁 {{ t.occasion }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>Seja a primeira a deixar seu feedback! ✨</p>
      </div>

      <!-- Formulário de envio -->
      <div class="feedback-form-wrap">
        <button class="btn-toggle-form" @click="showForm = !showForm">
          {{ showForm ? '✕ Fechar' : '✍️ Deixar meu feedback' }}
        </button>

        <transition name="slide">
          <form v-if="showForm" class="feedback-form" @submit.prevent="submitFeedback">
            <div class="form-row">
              <div class="form-group">
                <label>Seu nome *</label>
                <input v-model="form.customerName" type="text" placeholder="Ex: Ana S." required />
              </div>
              <div class="form-group">
                <label>Cidade</label>
                <input v-model="form.city" type="text" placeholder="Ex: Belo Horizonte" />
              </div>
            </div>

            <div class="form-group">
              <label>Avaliação *</label>
              <div class="star-picker">
                <button
                  v-for="n in 5" :key="n"
                  type="button"
                  class="star-btn"
                  :class="{ active: form.rating >= n }"
                  @click="form.rating = n"
                >⭐</button>
              </div>
            </div>

            <div class="form-group">
              <label>Ocasião (opcional)</label>
              <select v-model="form.occasion">
                <option value="">Nenhuma específica</option>
                <option value="Dia das Mães">Dia das Mães</option>
                <option value="Natal">Natal</option>
                <option value="Aniversário">Aniversário</option>
                <option value="Dia dos Namorados">Dia dos Namorados</option>
                <option value="Dia das Crianças">Dia das Crianças</option>
                <option value="Presente especial">Presente especial</option>
              </select>
            </div>

            <div class="form-group">
              <label>Mensagem *</label>
              <textarea
                v-model="form.message"
                placeholder="Conte sua experiência..."
                rows="3"
                required
                maxlength="500"
              />
              <span class="char-count">{{ form.message.length }}/500</span>
            </div>

            <p v-if="submitError" class="form-error">{{ submitError }}</p>
            <p v-if="submitSuccess" class="form-success">{{ submitSuccess }}</p>

            <button type="submit" class="btn-submit" :disabled="submitting">
              {{ submitting ? '⟳ Enviando...' : '💕 Enviar feedback' }}
            </button>
          </form>
        </transition>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'TestimonialsSection',
  data() {
    return {
      testimonials: [],
      showForm: false,
      submitting: false,
      submitError: '',
      submitSuccess: '',
      form: {
        customerName: '',
        city: '',
        rating: 5,
        occasion: '',
        message: ''
      }
    }
  },
  async mounted() {
    await this.loadTestimonials()
  },
  methods: {
    async loadTestimonials() {
      try {
        const res = await fetch('/api/social/testimonials')
        if (res.ok) this.testimonials = await res.json()
      } catch {}
    },
    async submitFeedback() {
      this.submitting = true
      this.submitError = ''
      this.submitSuccess = ''
      try {
        const res = await fetch('/api/social/testimonials', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            ...this.form,
            occasion: this.form.occasion || null
          })
        })
        const data = await res.json()
        if (res.ok) {
          this.submitSuccess = data.message || 'Feedback enviado! Obrigada 💕'
          this.form = { customerName: '', city: '', rating: 5, occasion: '', message: '' }
          setTimeout(() => { this.showForm = false; this.submitSuccess = '' }, 3000)
        } else {
          this.submitError = data.error || 'Erro ao enviar. Tente novamente.'
        }
      } catch {
        this.submitError = 'Erro de conexão. Tente novamente.'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.testimonials-section {
  padding: 60px 20px;
  background: white;
}

.section-inner {
  max-width: 1000px;
  margin: 0 auto;
}

.section-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.section-sub {
  text-align: center;
  color: #888;
  margin-bottom: 36px;
  font-size: 0.95rem;
}

/* Grid */
.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.testimonial-card {
  background: #fff5fb;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #fce4f0;
  transition: transform 0.2s, box-shadow 0.2s;
}
.testimonial-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(233,30,123,0.12);
}

.testimonial-img-wrap img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
}

.testimonial-body {
  padding: 20px;
}

.stars { font-size: 1rem; margin-bottom: 10px; }

.testimonial-msg {
  color: #444;
  font-size: 0.92rem;
  line-height: 1.6;
  margin-bottom: 14px;
  font-style: italic;
}

.testimonial-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.customer-name {
  font-weight: 700;
  color: #e91e7b;
  font-size: 0.9rem;
}

.customer-city {
  color: #888;
  font-size: 0.8rem;
}

.occasion-badge {
  background: #fce4f0;
  color: #c2185b;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  color: #aaa;
  padding: 40px;
}

/* Form */
.feedback-form-wrap {
  text-align: center;
}

.btn-toggle-form {
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 12px 28px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.2s;
}
.btn-toggle-form:hover { opacity: 0.85; }

.feedback-form {
  max-width: 560px;
  margin: 24px auto 0;
  background: #fff5fb;
  border-radius: 16px;
  padding: 28px;
  text-align: left;
  border: 1px solid #fce4f0;
}

.form-row {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}
.form-row .form-group { flex: 1; min-width: 140px; }

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
  position: relative;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #555;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 14px;
  border: 2px solid #f0d0e4;
  border-radius: 8px;
  font-size: 0.95rem;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
  background: white;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus { border-color: #e91e7b; }

.char-count {
  position: absolute;
  bottom: 8px;
  right: 12px;
  font-size: 0.75rem;
  color: #bbb;
}

/* Star picker */
.star-picker {
  display: flex;
  gap: 4px;
}
.star-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  opacity: 0.3;
  transition: opacity 0.15s, transform 0.15s;
  padding: 0;
}
.star-btn.active { opacity: 1; }
.star-btn:hover { transform: scale(1.2); }

.form-error {
  background: #fdecea;
  color: #c62828;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.form-success {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.btn-submit {
  width: 100%;
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 14px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.2s;
}
.btn-submit:hover:not(:disabled) { opacity: 0.9; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

/* Transition */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
