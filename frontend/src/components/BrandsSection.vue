<template>
  <section class="brands-section" id="sobre">
    <div class="container">
      <h2 class="section-title">💕 Quem Faz com Amor</h2>
      <p class="section-subtitle">Cada peça carrega um pedaço do nosso coração</p>

      <div class="brands-card">
        <div class="brand-item">
          <div class="brand-avatar">
            <span class="brand-emoji">🧶</span>
          </div>
          <div class="brand-info">
            <h3 class="brand-name">Maju & sua Mãe 💕</h3>
            <p class="brand-title">Ateliê Fios MJ · Artesanato em Crochê</p>
            <p class="brand-bio">
              O Fios MJ nasceu do amor entre mãe e filha e do prazer de criar peças únicas com as próprias mãos.
              Cada amigurumi, cada croppedm cada detalhe é feito com paciência, carinho e muito fio.
              Somos de Minas Gerais e acreditamos que o artesanato tem o poder de conectar pessoas. 🌸
            </p>
            <div class="brand-tags">
              <span class="brand-tag">🎨 Feito à mão</span>
              <span class="brand-tag">🇧🇷 Made in MG</span>
              <span class="brand-tag">💝 Com amor</span>
              <span class="brand-tag">🌿 100% algodão</span>
            </div>
            <div class="brand-social">
              <a href="https://instagram.com/fiosmjcroche" target="_blank" rel="noopener" class="social-link instagram">
                📸 @fiosmjcroche
              </a>
              <a href="https://wa.me/5533999892409" target="_blank" rel="noopener" class="social-link whatsapp">
                💬 Fale Conosco
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default { name: 'BrandsSection' }
</script>

<style scoped>
.brands-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #fff5fb 0%, #fce4f0 100%);
}

.brands-card {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 24px;
  padding: 48px;
  box-shadow: 0 8px 40px rgba(233,30,123,0.12);
}

.brand-item {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.brand-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fce4f0, #e91e7b22);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 4px solid #fce4f0;
  box-shadow: 0 4px 20px rgba(233,30,123,0.15);
}

.brand-emoji { font-size: 3.5rem; }

.brand-name {
  font-size: 1.8rem;
  color: #e91e7b;
  font-family: 'Playfair Display', serif;
  margin-bottom: 4px;
}

.brand-title {
  font-size: 0.9rem;
  color: #888;
  font-weight: 600;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.brand-bio {
  color: #555;
  line-height: 1.8;
  margin-bottom: 20px;
  font-size: 1rem;
}

.brand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.brand-tag {
  background: #fce4f0;
  color: #c2185b;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 0.82rem;
  font-weight: 600;
}

.brand-social {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.social-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.9rem;
  transition: opacity 0.2s, transform 0.1s;
}

.social-link:hover { opacity: 0.9; transform: translateY(-1px); }

.instagram {
  background: linear-gradient(135deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888);
  color: white;
}

.whatsapp {
  background: #25d366;
  color: white;
}

@media (max-width: 640px) {
  .brand-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 24px;
  }
  .brand-tags, .brand-social { justify-content: center; }
  .brands-card { padding: 28px 20px; }
}
</style>
