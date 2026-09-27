import { reactive } from 'vue'

// Textos e contatos editáveis pelo painel admin (aba "Site").
// Os valores padrão ficam no backend (SiteSettingsController).
const s = reactive({})

export async function loadSite() {
  try {
    const ctrl = new AbortController()
    const t = setTimeout(() => ctrl.abort(), 3000)
    const r = await fetch('/api/site', { signal: ctrl.signal })
    clearTimeout(t)
    if (r.ok) Object.assign(s, await r.json())
  } catch {}
}

const escape = str => String(str ?? '').replace(/[&<>"']/g, c =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))

export const site = {
  s,
  /** Link do WhatsApp com mensagem opcional */
  wa(msg) {
    const n = String(s.whatsapp || '').replace(/\D/g, '')
    return `https://wa.me/${n}` + (msg ? `?text=${encodeURIComponent(msg)}` : '')
  },
  get igUrl() { return `https://instagram.com/${s.instagram || ''}` },
  get igHandle() { return '@' + (s.instagram || '') },
  /** Texto seguro: **negrito** e quebras de linha */
  rich(text) {
    return escape(text).replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>')
  },
}
