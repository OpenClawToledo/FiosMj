import { reactive } from 'vue'

// ─────────────────────────────────────────────────────────────
// Tema de Natal
//  • Liga sozinho de 15/11 a 06/01 (data do aparelho de quem visita).
//  • Prévia: abra fiosmj.com/?natal=previa → o tema aparece por 2 horas,
//    só nesse aparelho. Para sair antes: ?natal=off ou o botão "Sair".
// ─────────────────────────────────────────────────────────────

const PREVIEW_KEY = 'fiosmj-natal-previa-ate'
const PREVIEW_MS = 2 * 60 * 60 * 1000

export const theme = reactive({
  natal: false,        // tema de Natal ativo agora
  preview: false,      // ativo por causa da prévia (não pela data)
  previewUntil: null,  // Date em que a prévia termina
})

/** 15/11 até 06/01, inclusive */
export function isChristmasSeason(d = new Date()) {
  const m = d.getMonth() + 1
  const day = d.getDate()
  return (m === 11 && day >= 15) || m === 12 || (m === 1 && day <= 6)
}

function readPreview() {
  try {
    const until = Number(localStorage.getItem(PREVIEW_KEY))
    if (until && until > Date.now()) return until
    if (until) localStorage.removeItem(PREVIEW_KEY)
  } catch {}
  return null
}

function handleUrl() {
  const params = new URLSearchParams(window.location.search)
  const v = params.get('natal')
  if (v === null) return
  try {
    if (v === 'off') localStorage.removeItem(PREVIEW_KEY)
    else localStorage.setItem(PREVIEW_KEY, String(Date.now() + PREVIEW_MS))
  } catch {}
  // limpa o parâmetro da barra de endereço (mantém o #hash)
  params.delete('natal')
  const qs = params.toString()
  history.replaceState(null, '', window.location.pathname + (qs ? '?' + qs : '') + window.location.hash)
}

function apply() {
  const until = readPreview()
  const season = isChristmasSeason()
  theme.preview = !!until && !season
  theme.previewUntil = until ? new Date(until) : null
  theme.natal = season || !!until
  document.documentElement.classList.toggle('theme-natal', theme.natal)
  const meta = document.querySelector('meta[name="theme-color"]')
  if (meta) meta.setAttribute('content', theme.natal ? '#6E1F2A' : '#ffffff')
}

export function endPreview() {
  try { localStorage.removeItem(PREVIEW_KEY) } catch {}
  apply()
}

export function initTheme() {
  handleUrl()
  apply()
  // reavalia a cada 30s: a prévia termina sozinha e o tema muda à meia-noite
  setInterval(apply, 30 * 1000)
  document.addEventListener('visibilitychange', () => { if (!document.hidden) apply() })
}
