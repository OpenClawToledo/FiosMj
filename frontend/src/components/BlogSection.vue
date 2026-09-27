<template>
  <section class="blog-section" id="blog">
    <div class="container">
      <h2 class="section-title">📝 Blog & Tutoriais</h2>
      <p class="section-subtitle">Aprenda, inspire-se e mergulhe no mundo do crochê artesanal!</p>

      <div v-if="loading" class="blog-loading">Carregando posts...</div>

      <div v-else-if="posts.length === 0" class="blog-empty">
        Em breve, tutoriais e dicas incríveis! 🧶
      </div>

      <div v-else class="blog-grid">
        <div
          v-for="post in posts"
          :key="post.id"
          class="blog-card"
          @click="$emit('open-post', post.slug)"
        >
          <div class="blog-image-wrap">
            <img
              v-if="post.imageUrl"
              :src="post.imageUrl"
              :alt="post.title"
              class="blog-image"
            />
            <div v-else class="blog-image-placeholder">🧶</div>
          </div>
          <div class="blog-content">
            <span class="blog-author">✍️ {{ post.authorName }}</span>
            <h3 class="blog-title">{{ post.title }}</h3>
            <p class="blog-excerpt">{{ post.excerpt }}</p>
            <span class="blog-read-more">Ler mais →</span>
          </div>
        </div>
      </div>

      <div v-if="posts.length > 0" class="blog-cta">
        <a href="#blog" @click.prevent="$emit('open-blog-list')" class="btn-blog">
          📚 Ver todos os posts
        </a>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'BlogSection',
  emits: ['open-post', 'open-blog-list'],
  data() {
    return {
      posts: [],
      loading: true
    }
  },
  async mounted() {
    try {
      const res = await fetch('/api/blog')
      if (res.ok) this.posts = (await res.json()).slice(0, 4)
    } catch {}
    this.loading = false
  }
}
</script>

<style scoped>
.blog-section {
  padding: 80px 0;
  background: white;
}

.blog-loading, .blog-empty {
  text-align: center;
  color: #aaa;
  padding: 40px;
  font-size: 1.05rem;
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 28px;
  margin-bottom: 36px;
}

.blog-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(233,30,123,0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.blog-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 30px rgba(233,30,123,0.18);
}

.blog-image-wrap {
  aspect-ratio: 16/9;
  overflow: hidden;
}

.blog-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.blog-card:hover .blog-image { transform: scale(1.05); }

.blog-image-placeholder {
  width: 100%;
  height: 100%;
  background: #fce4f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
}

.blog-content {
  padding: 20px;
}

.blog-author {
  font-size: 0.8rem;
  color: #e91e7b;
  font-weight: 600;
  display: block;
  margin-bottom: 8px;
}

.blog-title {
  font-size: 1.15rem;
  color: #1a1a2e;
  margin-bottom: 10px;
  font-family: 'Playfair Display', serif;
  line-height: 1.3;
}

.blog-excerpt {
  color: #666;
  font-size: 0.88rem;
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-read-more {
  color: #e91e7b;
  font-size: 0.88rem;
  font-weight: 700;
}

.blog-cta {
  text-align: center;
}

.btn-blog {
  display: inline-block;
  background: #e91e7b;
  color: white;
  padding: 12px 32px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.95rem;
  transition: opacity 0.2s, transform 0.1s;
}

.btn-blog:hover { opacity: 0.9; transform: translateY(-1px); }

@media (max-width: 768px) {
  .blog-section { padding: 50px 0; }
  .blog-grid { grid-template-columns: 1fr; gap: 20px; }
}
</style>
