<template>
  <div v-if="theme.preview" class="preview-badge" role="status">
    <span>🎄 Prévia de Natal · até {{ hora }}</span>
    <button type="button" @click="endPreview">Sair</button>
  </div>
</template>

<script>
import { theme, endPreview } from '../store/theme.js'

export default {
  name: 'ThemePreviewBadge',
  setup() {
    return { theme, endPreview }
  },
  computed: {
    hora() {
      const d = this.theme.previewUntil
      return d ? d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : ''
    },
  },
}
</script>

<style scoped>
.preview-badge {
  position: fixed;
  top: 82px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 250;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 6px 6px 14px;
  border-radius: 50px;
  background: #1F4238;
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.preview-badge button {
  border: none;
  border-radius: 50px;
  padding: 4px 12px;
  background: #F7E3B5;
  color: #1F4238;
  font: inherit;
  cursor: pointer;
}
</style>
