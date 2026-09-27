import { reactive, computed } from 'vue'

const state = reactive({
  customer: null,
  token: localStorage.getItem('fiosmj_token') || null
})

export function useAuth() {
  const isLoggedIn = computed(() => !!state.token && !!state.customer)

  const getHeaders = () => {
    const h = { 'Content-Type': 'application/json' }
    if (state.token) h['Authorization'] = `Bearer ${state.token}`
    return h
  }

  const setToken = (token) => {
    state.token = token
    if (token) localStorage.setItem('fiosmj_token', token)
    else localStorage.removeItem('fiosmj_token')
  }

  const login = async (email, password) => {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.error || 'Erro ao fazer login')
    setToken(data.token)
    state.customer = data.customer
    return data
  }

  const register = async (name, email, phone, password) => {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, phone, password })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.error || 'Erro ao criar conta')
    setToken(data.token)
    state.customer = data.customer
    return data
  }

  const logout = () => {
    setToken(null)
    state.customer = null
  }

  const loadMe = async () => {
    if (!state.token) return
    try {
      const res = await fetch('/api/auth/me', {
        headers: { 'Authorization': `Bearer ${state.token}` }
      })
      if (res.ok) {
        state.customer = await res.json()
      } else {
        logout()
      }
    } catch {
      logout()
    }
  }

  return {
    customer: state,
    isLoggedIn,
    getHeaders,
    login,
    register,
    logout,
    loadMe
  }
}
