<template>
  <div class="snow" aria-hidden="true">
    <span v-for="f in flakes" :key="f.i" class="flake" :style="f.style">❄</span>
  </div>
</template>

<script>
export default {
  name: 'Snowfall',
  data() {
    // posições fixas (pseudo-aleatórias) para não "saltar" a cada render
    const flakes = Array.from({ length: 22 }, (_, i) => {
      const r = n => ((i * 9301 + n * 49297) % 233280) / 233280
      return {
        i,
        style: {
          left: (r(1) * 100).toFixed(1) + '%',
          fontSize: (8 + r(2) * 12).toFixed(0) + 'px',
          opacity: (0.35 + r(3) * 0.5).toFixed(2),
          animationDuration: (9 + r(4) * 10).toFixed(1) + 's',
          animationDelay: (-r(5) * 18).toFixed(1) + 's',
          '--drift': ((r(6) - 0.5) * 80).toFixed(0) + 'px',
        },
      }
    })
    return { flakes }
  },
}
</script>

<style scoped>
.snow {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 2;
}
.flake {
  position: absolute;
  top: -24px;
  color: #fff;
  text-shadow: 0 0 6px rgba(255, 255, 255, 0.6);
  animation-name: fall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
  will-change: transform;
}
@keyframes fall {
  from { transform: translate3d(0, 0, 0) rotate(0deg); }
  to   { transform: translate3d(var(--drift), 110vh, 0) rotate(240deg); }
}
@media (prefers-reduced-motion: reduce) {
  .snow { display: none; }
}
</style>
