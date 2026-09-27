import { reactive, computed, watch } from 'vue'

const CART_KEY = 'fiosmj_cart'

// Load initial state from localStorage
function loadFromLocal() {
  try {
    const raw = localStorage.getItem(CART_KEY)
    if (raw) return JSON.parse(raw)
  } catch {}
  return []
}

const state = reactive({ items: loadFromLocal() })

// Watch for changes and save to localStorage
watch(
  () => [...state.items],
  (items) => {
    try {
      localStorage.setItem(CART_KEY, JSON.stringify(items))
    } catch {}
  },
  { deep: true }
)

const syncToServer = async (items) => {
  const token = localStorage.getItem('fiosmj_token')
  if (!token) return
  try {
    const payload = items.map(i => ({
      productId: i.product.id,
      productName: i.product.name,
      price: i.price,
      quantity: i.quantity,
      selectedSize: i.selectedSize?.size || null
    }))
    await fetch('/api/cart', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    })
  } catch {
    // silent fail - local cart still works
  }
}

export function useCart() {
  const addItem = (product, quantity = 1, selectedSize = null) => {
    const price = selectedSize ? selectedSize.price : product.price
    const key = `${product.id}-${selectedSize?.size || 'default'}`
    const existing = state.items.find(i => i.key === key)
    if (existing) {
      existing.quantity += quantity
    } else {
      state.items.push({ key, product, selectedSize, price, quantity })
    }
    localStorage.setItem(CART_KEY, JSON.stringify(state.items))
    syncToServer(state.items)
  }

  const removeItem = (key) => {
    const idx = state.items.findIndex(i => i.key === key)
    if (idx > -1) state.items.splice(idx, 1)
    localStorage.setItem(CART_KEY, JSON.stringify(state.items))
    syncToServer(state.items)
  }

  const updateQty = (key, delta) => {
    const item = state.items.find(i => i.key === key)
    if (!item) return
    item.quantity += delta
    if (item.quantity <= 0) {
      removeItem(key)
    } else {
      localStorage.setItem(CART_KEY, JSON.stringify(state.items))
      syncToServer(state.items)
    }
  }

  const clearCart = () => {
    state.items.splice(0)
    localStorage.removeItem(CART_KEY)
    const token = localStorage.getItem('fiosmj_token')
    if (token) {
      fetch('/api/cart', {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      }).catch(() => {})
    }
  }

  const loadFromServer = async () => {
    const token = localStorage.getItem('fiosmj_token')
    if (!token) return
    // If local cart has items, prioritize local and sync to server
    if (state.items.length > 0) {
      syncToServer(state.items)
      return
    }
    // Local is empty — try server
    try {
      const res = await fetch('/api/cart', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!res.ok) return
      const data = await res.json()
      if (data.items && data.items.length > 0) {
        for (const item of data.items) {
          const product = { id: item.productId, name: item.productName, price: item.price }
          const selectedSize = item.selectedSize ? { size: item.selectedSize, price: item.price } : null
          const key = `${item.productId}-${item.selectedSize || 'default'}`
          if (!state.items.find(i => i.key === key)) {
            state.items.push({ key, product, selectedSize, price: item.price, quantity: item.quantity })
          }
        }
        localStorage.setItem(CART_KEY, JSON.stringify(state.items))
      }
    } catch {
      // silent fail
    }
  }

  const total = computed(() => state.items.reduce((s, i) => s + i.price * i.quantity, 0))
  const count = computed(() => state.items.reduce((s, i) => s + i.quantity, 0))

  return { items: state.items, addItem, removeItem, updateQty, clearCart, loadFromServer, total, count }
}
