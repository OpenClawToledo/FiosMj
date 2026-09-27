<template>
  <div class="visit-counter" title="Visitas ao site">
    <span class="counter-icon">👁️</span>
    <span class="counter-digits">
      <span
        v-for="(digit, i) in displayDigits"
        :key="i"
        class="digit-slot"
        :class="{ rolling: rollingIdx === i }"
      >{{ digit }}</span>
    </span>
  </div>
</template>

<script>
export default {
  name: 'VisitCounter',
  data() {
    return {
      count: 0,
      displayDigits: ['0','0','0','0','0'],
      rollingIdx: -1,
    }
  },
  async mounted() {
    try {
      const alreadyCounted = localStorage.getItem('fiosmj_counted')
      let res
      if (alreadyCounted) {
        // Dispositivo já contado — só lê
        res = await fetch('/api/stats/visits')
      } else {
        // Primeira visita deste aparelho — incrementa e marca
        res = await fetch('/api/stats/visits', { method: 'POST' })
        if (res.ok) localStorage.setItem('fiosmj_counted', '1')
      }
      if (res.ok) {
        const data = await res.json()
        this.count = data.count
        this.animateTo(data.count)
      }
    } catch {}
  },
  methods: {
    animateTo(target) {
      const str = String(target).padStart(5, '0')
      const digits = str.split('')
      // Animate digit by digit from right
      digits.forEach((d, i) => {
        setTimeout(() => {
          this.rollingIdx = i
          this.displayDigits[i] = d
          this.displayDigits = [...this.displayDigits]
          setTimeout(() => { this.rollingIdx = -1 }, 300)
        }, i * 80)
      })
    }
  }
}
</script>

<style scoped>
.visit-counter {
  position: fixed;
  bottom: 90px;
  right: 16px;
  z-index: 996;
  background: rgba(255,255,255,0.92);
  border: 1.5px solid #e91e7b33;
  border-radius: 20px;
  padding: 5px 12px 5px 9px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 10px rgba(233,30,123,0.12);
  backdrop-filter: blur(6px);
  font-family: 'Courier New', monospace;
  pointer-events: none;
  user-select: none;
  animation: float-counter 3s ease-in-out infinite;
}

@keyframes float-counter {
  0%, 100% { transform: translateY(0px); }
  50%       { transform: translateY(-5px); }
}

.counter-icon {
  font-size: 0.85rem;
  opacity: 0.8;
}

.counter-digits {
  display: flex;
  gap: 1px;
}

.digit-slot {
  display: inline-block;
  width: 13px;
  text-align: center;
  font-size: 0.82rem;
  font-weight: 700;
  color: #c2185b;
  transition: transform 0.2s ease, color 0.2s ease;
  letter-spacing: 0;
}

.digit-slot.rolling {
  transform: translateY(-3px) scale(1.2);
  color: #e91e7b;
}

@media (max-width: 600px) {
  .visit-counter {
    bottom: 80px;
    right: 10px;
    padding: 4px 9px 4px 7px;
  }
  .digit-slot { font-size: 0.75rem; width: 11px; }
}
</style>
