// Status de disponibilidade dos produtos (definido no painel admin)
export const AVAILABILITY = {
  PRONTA_ENTREGA: { label: 'Pronta entrega', icon: '✅', cls: 'ready' },
  SOB_ENCOMENDA: { label: 'Sob encomenda', icon: '🧶', cls: 'order' },
  SOB_CONSULTA: { label: 'Sob consulta', icon: '💬', cls: 'ask' },
  INDISPONIVEL: { label: 'Indisponível', icon: '⏸️', cls: 'off' }
}

export function availabilityOf(product) {
  return AVAILABILITY[product?.availability] || AVAILABILITY.SOB_ENCOMENDA
}

/** Pode ir para o carrinho: não esgotado, não indisponível e com preço definido. */
export function canBuy(product) {
  if (!product) return false
  if (product.stock === 0) return false
  return product.availability !== 'INDISPONIVEL' && product.availability !== 'SOB_CONSULTA'
}

/** Link público do produto, que abre com foto e texto próprios no WhatsApp/Instagram. */
export function productLink(product) {
  return `${window.location.origin}/produto/${product.id}`
}
