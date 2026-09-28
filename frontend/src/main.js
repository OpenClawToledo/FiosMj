import { createApp } from 'vue'
import App from './App.vue'
import './assets/styles.css'
import './assets/natal.css'
import { site, loadSite } from './store/site'
import { initTheme } from './store/theme'

initTheme()

loadSite().finally(() => {
  const app = createApp(App)
  app.config.globalProperties.$site = site
  app.mount('#app')
})
