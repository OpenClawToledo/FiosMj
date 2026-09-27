<template>
  <section id="sobre" class="about-section">
    <div class="container">
      <h2 class="section-title">{{ $site.s.aboutTitle }}</h2>
      <p class="section-subtitle">{{ $site.s.aboutSubtitle }}</p>

      <div class="about-content">
        <p class="about-text" v-if="$site.s.aboutText1" v-html="$site.rich($site.s.aboutText1)"></p>
        <p class="about-text" v-if="$site.s.aboutText2" v-html="$site.rich($site.s.aboutText2)"></p>
        <p class="about-text highlight" v-if="$site.s.aboutHighlight">{{ $site.s.aboutHighlight }}</p>
      </div>

      <div class="about-stats">
        <div class="stat" v-for="i in [1, 2, 3]" :key="i" v-show="$site.s['stat' + i + 'Number']">
          <span class="stat-number">{{ $site.s['stat' + i + 'Number'] }}</span>
          <span class="stat-label">{{ $site.s['stat' + i + 'Label'] }}</span>
        </div>
      </div>

      <div class="about-social">
        <h3 class="social-heading">Nos siga nas redes!</h3>
        <div class="social-buttons">
          <a :href="$site.igUrl" target="_blank" rel="noopener" class="social-btn instagram">
            📸 {{ $site.igHandle }}
          </a>
          <a :href="$site.wa()" target="_blank" rel="noopener" class="social-btn whatsapp">
            💬 WhatsApp
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default { name: 'AboutSection' }
</script>

<style scoped>
.about-section {
  padding: 80px 0;
  background: linear-gradient(135deg, var(--pink-light) 0%, #fff 100%);
}

.about-content {
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.about-text {
  color: var(--text-dark);
  font-size: 1.1rem;
  margin-bottom: 16px;
  line-height: 1.8;
}

.about-text.highlight {
  font-size: 1.25rem;
  font-style: italic;
  color: var(--pink-dark);
  margin-top: 24px;
}

.about-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin: 40px 0;
  padding: 30px 0;
  border-top: 1px solid rgba(233, 30, 123, 0.15);
  border-bottom: 1px solid rgba(233, 30, 123, 0.15);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--pink-primary);
  font-family: var(--font-heading);
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-light);
  margin-top: 8px;
  font-weight: 500;
}

.about-social {
  text-align: center;
  margin-top: 40px;
}

.social-heading {
  font-family: var(--font-heading);
  font-size: 1.4rem;
  color: var(--pink-dark);
  margin-bottom: 20px;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.social-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.05rem;
  transition: var(--transition);
}

.social-btn.instagram {
  background: linear-gradient(135deg, #e91e7b, #c2185b);
  color: white;
}

.social-btn.instagram:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(233, 30, 123, 0.4);
}

.social-btn.whatsapp {
  background: #25D366;
  color: white;
}

.social-btn.whatsapp:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(37, 211, 102, 0.4);
}

@media (max-width: 768px) {
  .about-section {
    padding: 50px 0;
  }

  .about-stats {
    gap: 30px;
  }

  .stat-number {
    font-size: 2rem;
  }

  .social-btn {
    font-size: 0.95rem;
    padding: 12px 22px;
  }
}
</style>
