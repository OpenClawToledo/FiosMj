<template>
  <section class="how-section" id="como-pedir">
    <div class="container">
      <p class="section-eyebrow">📦 Simples e rápido</p>
      <h2 class="section-title">Como Encomendar</h2>
      <p class="section-subtitle">
        {{ site.s.howSubtitle }}
      </p>

      <div class="steps-grid">
        <div class="step" v-for="(step, i) in steps" :key="i">
          <div class="step-number">{{ i + 1 }}</div>
          <div class="step-icon">{{ step.icon }}</div>
          <h3 class="step-title">{{ step.title }}</h3>
          <p class="step-desc">{{ step.desc }}</p>
        </div>
      </div>

      <div class="how-cta">
        <a
          :href="site.wa('Olá! Quero fazer uma encomenda na Fios MJ 🧶')"
          target="_blank"
          rel="noopener noreferrer"
          class="btn-primary"
        >
          🧶 Fazer Minha Encomenda
        </a>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { site } from '../store/site'

const icons = ['👀', '💬', '🎁']
const steps = computed(() => icons.map((icon, i) => ({
  icon,
  title: site.s[`step${i + 1}Title`],
  desc: site.s[`step${i + 1}Desc`],
})).filter(st => st.title))
</script>

<style scoped>
.how-section {
  padding: 80px 0;
  background: linear-gradient(135deg, var(--pink-pale) 0%, var(--pink-light) 100%);
}

.section-eyebrow {
  text-align: center;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pink-primary);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  margin-bottom: 48px;
  position: relative;
}

.steps-grid::before {
  content: '';
  position: absolute;
  top: 52px;
  left: calc(16.66% + 16px);
  right: calc(16.66% + 16px);
  height: 2px;
  background: linear-gradient(to right, var(--pink-primary), var(--pink-soft));
  z-index: 0;
}

.step {
  background: white;
  border-radius: var(--radius-md);
  padding: 36px 28px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 1;
  transition: var(--transition);
}

.step:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.step-number {
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 32px;
  background: var(--pink-primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
}

.step-icon {
  font-size: 3rem;
  margin: 12px 0 16px;
}

.step-title {
  font-family: var(--font-heading);
  font-size: 1.15rem;
  color: var(--gray-dark);
  margin-bottom: 10px;
}

.step-desc {
  font-size: 0.9rem;
  color: var(--gray-mid);
  line-height: 1.6;
}

.how-cta {
  text-align: center;
}

@media (max-width: 768px) {
  .steps-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .steps-grid::before {
    display: none;
  }
}
</style>
