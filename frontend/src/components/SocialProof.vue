<template>
  <div class="social-proof-root">

    <!-- Bolhas de visitantes activos (canto esquerdo) -->
    <div class="active-bubbles" v-if="visitors.length > 0">
      <div class="bubble-label">{{ count }} a ver agora</div>
      <div class="bubble-stack">
        <transition-group name="bubble-pop" tag="div" class="bubble-row">
          <div
            v-for="v in visitors.slice(0, 6)"
            :key="v.sessionId"
            class="visitor-bubble"
            :style="{ background: v.color }"
            :title="v.label"
          >{{ v.initial }}</div>
        </transition-group>
      </div>
    </div>

    <!-- Balões de compra a subir -->
    <transition-group name="toast-rise" tag="div" class="toast-container">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast-balloon"
      >
        <span class="toast-text">{{ t.label }}</span>
        <span class="toast-ago">{{ t.ago }}</span>
      </div>
    </transition-group>

  </div>
</template>

<script>
const SESSION_KEY = 'fiosmj_session_id'

function getOrCreateSession() {
  let id = localStorage.getItem(SESSION_KEY)
  if (!id) {
    id = 'anon_' + Math.random().toString(36).slice(2, 10)
    localStorage.setItem(SESSION_KEY, id)
  }
  return id
}

let pingInterval = null
let activeInterval = null
let purchaseInterval = null
let toastIdCounter = 0

export default {
  name: 'SocialProof',
  props: {
    userName: { type: String, default: null } // pass logged-in user name if available
  },
  data() {
    return {
      count: 0,
      visitors: [],
      toasts: [],
      seenPurchaseLabels: new Set()
    }
  },
  mounted() {
    this.ping()
    this.fetchActive()
    this.fetchPurchases()

    pingInterval    = setInterval(() => this.ping(),          30_000)
    activeInterval  = setInterval(() => this.fetchActive(),   20_000)
    purchaseInterval = setInterval(() => this.fetchPurchases(), 15_000)
  },
  beforeUnmount() {
    clearInterval(pingInterval)
    clearInterval(activeInterval)
    clearInterval(purchaseInterval)
  },
  methods: {
    async ping() {
      try {
        await fetch('/api/presence/ping', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            sessionId: getOrCreateSession(),
            displayName: this.userName || null
          })
        })
      } catch {}
    },

    async fetchActive() {
      try {
        const res = await fetch('/api/presence/active')
        if (!res.ok) return
        const data = await res.json()
        this.count = data.count || 0
        this.visitors = data.visitors || []
      } catch {}
    },

    async fetchPurchases() {
      try {
        const res = await fetch('/api/presence/recent-purchases')
        if (!res.ok) return
        const purchases = await res.json()
        purchases.forEach(p => {
          if (!this.seenPurchaseLabels.has(p.label)) {
            this.seenPurchaseLabels.add(p.label)
            this.showToast(p.label, p.ago)
          }
        })
      } catch {}
    },

    showToast(label, ago) {
      const id = ++toastIdCounter
      this.toasts.push({ id, label, ago })
      // Remove after 5s
      setTimeout(() => {
        this.toasts = this.toasts.filter(t => t.id !== id)
      }, 5000)
    }
  }
}
</script>

<style scoped>
.social-proof-root {
  pointer-events: none;
}

/* ── Bolhas activas ── */
.active-bubbles {
  position: fixed;
  bottom: 80px;
  left: 16px;
  z-index: 998;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.bubble-label {
  font-size: 0.7rem;
  color: #888;
  background: rgba(255,255,255,0.85);
  padding: 2px 7px;
  border-radius: 10px;
  border: 1px solid #eee;
  backdrop-filter: blur(4px);
  white-space: nowrap;
}

.bubble-row {
  display: flex;
  gap: -8px;
}

.visitor-bubble {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 2.5px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
  color: white;
  margin-left: -8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
  cursor: default;
}

.visitor-bubble:first-child { margin-left: 0; }

/* ── Balões de compra ── */
.toast-container {
  position: fixed;
  bottom: 130px;
  left: 16px;
  z-index: 997;
  display: flex;
  flex-direction: column-reverse;
  gap: 8px;
  pointer-events: none;
}

.toast-balloon {
  background: white;
  border: 1.5px solid #f8bbd0;
  border-radius: 16px 16px 16px 4px;
  padding: 8px 14px;
  box-shadow: 0 4px 16px rgba(233,30,123,0.15);
  max-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toast-text {
  font-size: 0.78rem;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.toast-ago {
  font-size: 0.68rem;
  color: #aaa;
}

/* ── Animações ── */
.bubble-pop-enter-active { transition: all 0.4s cubic-bezier(0.34,1.56,0.64,1); }
.bubble-pop-enter-from   { transform: scale(0); opacity: 0; }

.toast-rise-enter-active {
  animation: balloon-rise 0.5s cubic-bezier(0.34,1.56,0.64,1) forwards;
}
.toast-rise-leave-active {
  animation: balloon-fade 0.6s ease forwards;
}

@keyframes balloon-rise {
  from { transform: translateY(20px) scale(0.85); opacity: 0; }
  to   { transform: translateY(0)    scale(1);    opacity: 1; }
}
@keyframes balloon-fade {
  from { transform: translateY(0)    scale(1);    opacity: 1; }
  to   { transform: translateY(-20px) scale(0.9); opacity: 0; }
}

@media (max-width: 600px) {
  .active-bubbles { bottom: 70px; left: 10px; }
  .toast-container { bottom: 120px; left: 10px; }
  .visitor-bubble { width: 28px; height: 28px; font-size: 0.75rem; }
}
</style>
