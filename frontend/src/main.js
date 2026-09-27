import { createApp } from 'vue'
import App from './App.vue'
import './assets/styles.css'
import { site, loadSite } from './store/site'

loadSite().finally(() => {
  const app = createApp(App)
  app.config.globalProperties.$site = site
  app.mount('#app')
})
