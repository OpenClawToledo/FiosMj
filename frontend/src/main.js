import { createApp } from 'vue'
import App from './App.vue'
import './assets/styles.css'
import './assets/natal.css'
import { site, loadSite } from './store/site'
import { initTheme } from './store/theme'

initTheme()

// App instalável: registra o service worker (só no site publicado)
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js').catch(() => {}))
}

loadSite().finally(() => {
  const app = createApp(App)
  app.config.globalProperties.$site = site
  app.mount('#app')
})
