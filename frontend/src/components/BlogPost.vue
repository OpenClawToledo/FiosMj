<template>
  <div class="blog-post-page">
    <div class="container">
      <button class="btn-back" @click="$emit('go-back')">← Voltar ao Blog</button>

      <div v-if="loading" class="post-loading">Carregando post...</div>

      <div v-else-if="!post" class="post-not-found">
        <h2>Post não encontrado 😔</h2>
        <button class="btn-back" @click="$emit('go-back')">← Voltar</button>
      </div>

      <article v-else class="post-article">
        <div v-if="post.imageUrl" class="post-hero">
          <img :src="post.imageUrl" :alt="post.title" class="post-hero-img" />
        </div>

        <div class="post-meta">
          <span class="post-author">✍️ {{ post.authorName }}</span>
          <span class="post-date">📅 {{ formatDate(post.createdAt) }}</span>
        </div>

        <h1 class="post-title">{{ post.title }}</h1>

        <p v-if="post.excerpt" class="post-excerpt">{{ post.excerpt }}</p>

        <div class="post-content" v-html="post.content"></div>

        <div class="post-footer">
          <button class="btn-back-bottom" @click="$emit('go-back')">← Voltar ao Blog</button>
          <a
            href="https://instagram.com/fiosmjcroche"
            target="_blank" rel="noopener"
            class="btn-instagram"
          >📸 Seguir @fiosmjcroche</a>
        </div>
      </article>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BlogPost',
  props: {
    slug: { type: String, required: true }
  },
  emits: ['go-back'],
  data() {
    return {
      post: null,
      loading: true
    }
  },
  methods: {
    formatDate(dt) {
      if (!dt) return ''
      return new Date(dt).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' })
    }
  },
  async mounted() {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    try {
      const res = await fetch(`/api/blog/${this.slug}`)
      if (res.ok) this.post = await res.json()
    } catch {}
    this.loading = false
  },
  watch: {
    slug: {
      async handler(slug) {
        this.loading = true
        this.post = null
        window.scrollTo({ top: 0, behavior: 'smooth' })
        try {
          const res = await fetch(`/api/blog/${slug}`)
          if (res.ok) this.post = await res.json()
        } catch {}
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.blog-post-page {
  padding: 100px 0 60px;
  min-height: 100vh;
  background: #fff5fb;
}

.btn-back {
  background: none;
  border: none;
  color: #e91e7b;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
  margin-bottom: 28px;
  font-family: inherit;
  display: inline-block;
}

.btn-back:hover { text-decoration: underline; }

.post-loading, .post-not-found {
  text-align: center;
  padding: 60px;
  color: #aaa;
}

.post-hero {
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 28px;
  max-height: 400px;
}

.post-hero-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.post-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.post-author, .post-date {
  font-size: 0.88rem;
  color: #888;
  font-weight: 600;
}

.post-author { color: #e91e7b; }

.post-title {
  font-size: 2.2rem;
  color: #1a1a2e;
  font-family: 'Playfair Display', serif;
  line-height: 1.2;
  margin-bottom: 16px;
}

.post-excerpt {
  font-size: 1.1rem;
  color: #666;
  font-style: italic;
  border-left: 4px solid #e91e7b;
  padding-left: 16px;
  margin-bottom: 32px;
  line-height: 1.7;
}

.post-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(233,30,123,0.08);
  line-height: 1.8;
  color: #444;
  margin-bottom: 40px;
}

.post-content :deep(h2) {
  color: #e91e7b;
  margin: 24px 0 12px;
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
}

.post-content :deep(h3) {
  color: #c2185b;
  margin: 20px 0 10px;
  font-size: 1.2rem;
}

.post-content :deep(p) {
  margin-bottom: 14px;
}

.post-content :deep(ul), .post-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 14px;
}

.post-content :deep(li) { margin-bottom: 6px; }

.post-content :deep(strong) { color: #333; font-weight: 700; }

.post-footer {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.btn-back-bottom {
  background: none;
  border: 2px solid #e91e7b;
  color: #e91e7b;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.btn-back-bottom:hover { background: #e91e7b; color: white; }

.btn-instagram {
  display: inline-block;
  background: linear-gradient(135deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888);
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 0.95rem;
  transition: opacity 0.2s;
}

.btn-instagram:hover { opacity: 0.9; }

@media (max-width: 768px) {
  .post-title { font-size: 1.5rem; }
  .post-content { padding: 20px; }
}
</style>
