<template>
  <section class="ranking-section">
    <div class="section-inner">
      <h2 class="section-title">🏆 Fãs da Maju</h2>
      <p class="section-sub">Clientes que já fazem parte da nossa família 💕</p>

      <div class="ranking-list" v-if="ranking.length">
        <div
          v-for="(c, i) in ranking"
          :key="i"
          class="ranking-card"
          :class="getBadgeClass(i)"
        >
          <div class="rank-number">{{ i + 1 }}º</div>

          <div class="rank-avatar">
            {{ initials(c.displayName) }}
          </div>

          <div class="rank-info">
            <div class="rank-name">
              {{ c.displayName }}
              <span class="rank-badge">{{ c.badge }}</span>
            </div>
            <div class="rank-city">📍 {{ c.city }}</div>
            <div class="rank-stats">
              <span>🛒 {{ c.orderCount }} {{ c.orderCount === 1 ? 'pedido' : 'pedidos' }}</span>
              <span v-if="c.lastProduct">· Último: {{ c.lastProduct }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sem dados ainda -->
      <div v-else class="empty-ranking">
        <div class="empty-icon">🧶</div>
        <p>Seja a primeira cliente do ranking!</p>
        <p class="empty-sub">Faça seu pedido e ganhe destaque aqui 💕</p>
      </div>

      <!-- CTA para se registrar -->
      <div class="ranking-cta">
        <p>Faça parte do ranking — <strong>crie sua conta</strong> e acompanhe suas compras!</p>
        <button class="btn-join" @click="$emit('open-auth')">
          👤 Criar conta grátis
        </button>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'CustomerRanking',
  emits: ['open-auth'],
  data() {
    return {
      ranking: []
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/social/ranking')
      if (res.ok) this.ranking = await res.json()
    } catch {}
  },
  methods: {
    initials(name) {
      if (!name) return '?'
      return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
    },
    getBadgeClass(i) {
      if (i === 0) return 'gold'
      if (i === 1) return 'silver'
      if (i === 2) return 'bronze'
      return ''
    }
  }
}
</script>

<style scoped>
.ranking-section {
  padding: 60px 20px;
  background: linear-gradient(135deg, #fce4f0 0%, #fff5fb 100%);
}

.section-inner {
  max-width: 680px;
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
}

/* Ranking list */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 36px;
}

.ranking-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 14px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.2s;
}
.ranking-card:hover { transform: translateX(4px); }

.ranking-card.gold { border-left: 4px solid #FFD700; }
.ranking-card.silver { border-left: 4px solid #C0C0C0; }
.ranking-card.bronze { border-left: 4px solid #CD7F32; }

.rank-number {
  font-size: 1.1rem;
  font-weight: 800;
  color: #ccc;
  min-width: 28px;
}
.gold .rank-number { color: #FFD700; }
.silver .rank-number { color: #aaa; }
.bronze .rank-number { color: #CD7F32; }

.rank-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e91e7b, #c2185b);
  color: white;
  font-weight: 700;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rank-info { flex: 1; }

.rank-name {
  font-weight: 700;
  color: #333;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.rank-badge {
  font-size: 0.75rem;
  background: #fce4f0;
  color: #c2185b;
  padding: 2px 8px;
  border-radius: 10px;
}

.rank-city {
  color: #999;
  font-size: 0.8rem;
  margin: 2px 0;
}

.rank-stats {
  color: #888;
  font-size: 0.82rem;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Empty */
.empty-ranking {
  text-align: center;
  padding: 40px 20px;
}
.empty-icon { font-size: 3rem; margin-bottom: 12px; }
.empty-ranking p { color: #666; font-size: 1rem; margin-bottom: 4px; }
.empty-sub { color: #aaa; font-size: 0.88rem; }

/* CTA */
.ranking-cta {
  text-align: center;
  background: white;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(233,30,123,0.08);
}
.ranking-cta p { color: #555; margin-bottom: 14px; font-size: 0.95rem; }
.btn-join {
  background: #e91e7b;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 12px 28px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.2s, transform 0.2s;
}
.btn-join:hover { opacity: 0.9; transform: translateY(-2px); }

@media (max-width: 480px) {
  .ranking-card { padding: 12px 14px; gap: 10px; }
  .rank-avatar { width: 40px; height: 40px; font-size: 0.85rem; }
}
</style>
