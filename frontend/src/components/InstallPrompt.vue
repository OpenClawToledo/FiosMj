<template>
  <transition name="install-slide">
    <div v-if="visible" class="install-bar" role="dialog" aria-label="Instalar o app Fios MJ">
      <img src="/icons/icon-192.png" alt="" class="install-icon" />
      <div class="install-text">
        <strong>App Fios MJ</strong>
        <span v-if="mode === 'ios'">Toque em <b>Compartilhar</b> e depois em <b>Adicionar à Tela de Início</b>.</span>
        <span v-else>Veja as peças e compre direto pelo celular.</span>
      </div>
      <button v-if="mode === 'prompt'" class="install-btn" @click="install">Instalar</button>
      <button class="install-close" aria-label="Fechar" @click="dismiss">✕</button>
    </div>
  </transition>
</template>

<script>
const KEY = 'fiosmj_install_dismissed'
const DAYS = 14

function dismissedRecently() {
  try {
    const t = Number(localStorage.getItem(KEY) || 0)
    return t && Date.now() - t < DAYS * 86400000
  } catch { return false }
}

export default {
  name: 'InstallPrompt',
  data() {
    return { visible: false, mode: null, deferred: null }
  },
  mounted() {
    const standalone = window.matchMedia('(display-mode: standalone)').matches || navigator.standalone === true
    if (standalone || dismissedRecently()) return

    this.onPrompt = e => {
      e.preventDefault()
      this.deferred = e
      this.mode = 'prompt'
      this.showLater()
    }
    window.addEventListener('beforeinstallprompt', this.onPrompt)

    // iPhone/iPad no Safari não tem botão de instalar: mostramos a instrução
    const ua = navigator.userAgent
    const ios = /iphone|ipad|ipod/i.test(ua) || (ua.includes('Mac') && 'ontouchend' in document)
    const safari = /safari/i.test(ua) && !/crios|fxios|edgios/i.test(ua)
    if (ios && safari) {
      this.mode = 'ios'
      this.showLater()
    }
  },
  beforeUnmount() {
    if (this.onPrompt) window.removeEventListener('beforeinstallprompt', this.onPrompt)
    clearTimeout(this.timer)
  },
  methods: {
    // Espera a cliente olhar a loja antes de oferecer o app
    showLater() {
      clearTimeout(this.timer)
      this.timer = setTimeout(() => { this.visible = true }, 15000)
    },
    async install() {
      if (!this.deferred) return
      this.deferred.prompt()
      await this.deferred.userChoice.catch(() => {})
      this.deferred = null
      this.visible = false
    },
    dismiss() {
      this.visible = false
      try { localStorage.setItem(KEY, String(Date.now())) } catch { /* sem armazenamento */ }
    }
  }
}
</script>

<style scoped>
.install-bar {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: calc(12px + env(safe-area-inset-bottom));
  z-index: 900;
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 16px;
  padding: 12px 12px 12px 14px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
  max-width: 520px;
  margin: 0 auto;
}
.install-icon { width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0; }
.install-text { flex: 1; display: flex; flex-direction: column; font-size: 0.85rem; color: #555; line-height: 1.35; }
.install-text strong { color: #c2185b; font-size: 0.95rem; }
.install-btn {
  background: #e91e7b; color: white; border: none; border-radius: 10px;
  padding: 10px 16px; font-weight: 700; font-family: inherit; cursor: pointer;
}
.install-close { background: none; border: none; color: #999; font-size: 1.1rem; padding: 8px; cursor: pointer; }
.install-slide-enter-active, .install-slide-leave-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.install-slide-enter-from, .install-slide-leave-to { transform: translateY(120%); opacity: 0; }
</style>
