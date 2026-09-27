<template>
  <div id="fiosmj-app">
    <VisitCounter />
    <SocialProof :user-name="authState && authState.customer ? authState.customer.name : null" />

    <!-- ─── PRODUCT PAGE ─── -->
    <template v-if="currentView.name === 'product' && currentProduct">
      <AppHeader
        :products="products"
        @open-cart="cartOpen = true"
        @open-auth="authOpen = true"
        @open-orders="ordersOpen = true"
        @open-product="openProduct"
      />
      <ProductPage
        :product="currentProduct"
        @go-home="goHome"
      />
      <AppFooter />
      <WhatsAppFloat />
    </template>

    <!-- ─── BLOG POST PAGE ─── -->
    <template v-else-if="currentView.name === 'blog-post'">
      <AppHeader
        :products="products"
        @open-cart="cartOpen = true"
        @open-auth="authOpen = true"
        @open-orders="ordersOpen = true"
        @open-product="openProduct"
      />
      <BlogPost
        :slug="currentView.slug"
        @go-back="goToBlog"
      />
      <AppFooter />
      <WhatsAppFloat />
    </template>

    <!-- ─── BLOG LIST PAGE ─── -->
    <template v-else-if="currentView.name === 'blog'">
      <AppHeader
        :products="products"
        @open-cart="cartOpen = true"
        @open-auth="authOpen = true"
        @open-orders="ordersOpen = true"
        @open-product="openProduct"
      />
      <div class="blog-list-page">
        <div class="container">
          <button class="btn-back-home" @click="goHome">← Voltar à Loja</button>
          <h1 class="page-title">📝 Blog & Tutoriais</h1>
          <BlogSection
            @open-post="openBlogPost"
            @open-blog-list="goToBlog"
          />
        </div>
      </div>
      <AppFooter />
      <WhatsAppFloat />
    </template>

    <!-- ─── HOME ─── -->
    <template v-else>
      <AppHeader
        :products="products"
        @open-cart="cartOpen = true"
        @open-auth="authOpen = true"
        @open-orders="ordersOpen = true"
        @open-product="openProduct"
      />
      <HeroSection />
      <ProductGrid :products="products" @open-product="openProduct" />
      <HowToOrder />
      <BrandsSection />
      <BlogSection
        @open-post="openBlogPost"
        @open-blog-list="goToBlog"
      />
      <InstagramCarousel />
      <TestimonialsSection />
      <GiftShowcase />
      <CustomerRanking @open-auth="authOpen = true" />
      <AboutSection />
      <AppFooter />
      <WhatsAppFloat />
    </template>

    <!-- Cart Drawer -->
    <CartDrawer
      :open="cartOpen"
      @close="cartOpen = false"
      @checkout="openCheckout"
      @open-auth="authOpen = true"
    />

    <!-- Checkout Form -->
    <CheckoutForm
      v-if="checkoutOpen"
      @close="checkoutOpen = false"
    />

    <!-- Auth Modal -->
    <AuthModal
      v-if="authOpen"
      @close="authOpen = false"
      @logged-in="authOpen = false"
    />

    <!-- Order History -->
    <OrderHistory
      v-if="ordersOpen"
      @close="ordersOpen = false"
    />
  </div>
</template>

<script>
import AppHeader from './components/AppHeader.vue'
import HeroSection from './components/HeroSection.vue'
import ProductGrid from './components/ProductGrid.vue'
import HowToOrder from './components/HowToOrder.vue'
import AboutSection from './components/AboutSection.vue'
import AppFooter from './components/AppFooter.vue'
import WhatsAppFloat from './components/WhatsAppFloat.vue'
import CartDrawer from './components/CartDrawer.vue'
import CheckoutForm from './components/CheckoutForm.vue'
import AuthModal from './components/AuthModal.vue'
import OrderHistory from './components/OrderHistory.vue'
import InstagramCarousel from './components/InstagramCarousel.vue'
import TestimonialsSection from './components/TestimonialsSection.vue'
import GiftShowcase from './components/GiftShowcase.vue'
import CustomerRanking from './components/CustomerRanking.vue'
import ProductPage from './components/ProductPage.vue'
import BlogSection from './components/BlogSection.vue'
import BlogPost from './components/BlogPost.vue'
import VisitCounter from './components/VisitCounter.vue'
import SocialProof from './components/SocialProof.vue'
import BrandsSection from './components/BrandsSection.vue'
import { useAuth } from './store/auth.js'

export default {
  name: 'App',
  components: {
    AppHeader,
    HeroSection,
    ProductGrid,
    HowToOrder,
    AboutSection,
    AppFooter,
    WhatsAppFloat,
    CartDrawer,
    CheckoutForm,
    AuthModal,
    OrderHistory,
    InstagramCarousel,
    TestimonialsSection,
    GiftShowcase,
    CustomerRanking,
    ProductPage,
    BlogSection,
    BlogPost,
    VisitCounter,
    SocialProof,
    BrandsSection
  },
  setup() {
    const auth = useAuth()
    return { loadMe: auth.loadMe, authState: auth.customer }
  },
  data() {
    return {
      products: [],
      cartOpen: false,
      checkoutOpen: false,
      authOpen: false,
      ordersOpen: false,
      currentView: { name: 'home' },
      currentProduct: null
    }
  },
  methods: {
    parseHash() {
      const hash = window.location.hash || ''
      // #/produto/:id
      const productMatch = hash.match(/^#\/produto\/(\d+)$/)
      if (productMatch) {
        const id = parseInt(productMatch[1])
        const product = this.products.find(p => p.id === id)
        if (product) {
          this.currentProduct = product
          this.currentView = { name: 'product', id }
          return
        }
      }
      // #/blog/:slug
      const blogPostMatch = hash.match(/^#\/blog\/(.+)$/)
      if (blogPostMatch) {
        this.currentView = { name: 'blog-post', slug: blogPostMatch[1] }
        return
      }
      // #/blog
      if (hash === '#/blog') {
        this.currentView = { name: 'blog' }
        return
      }
      this.currentView = { name: 'home' }
      this.currentProduct = null
    },
    openBlogPost(slug) {
      window.location.hash = '#/blog/' + slug
    },
    openProduct(product) {
      window.location.hash = `#/produto/${product.id}`
    },
    goHome() {
      window.location.hash = ''
      this.currentView = { name: 'home' }
      this.currentProduct = null
      this.$nextTick(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' })
      })
    },
    goToBlog() {
      window.location.hash = '#/blog'
    },
    openCheckout() {
      this.cartOpen = false
      this.checkoutOpen = true
    }
  },
  async mounted() {
    // Restore auth session if token exists
    await this.loadMe()

    try {
      const res = await fetch('/api/products')
      this.products = await res.json()
    } catch (e) {
      this.products = [
        {
          id: 1,
          name: 'Cropped Halter Brasil 🇧🇷',
          description: 'Cropped de crochê com tema Copa do Mundo 2026!',
          price: 100.0,
          imageUrl: '/img/cropped-brasil.jpg',
          category: 'Roupas',
          stock: 5,
          sizes: [
            { size: 'P', price: 100 },
            { size: 'M', price: 120 },
            { size: 'G', price: 150 }
          ]
        },
        {
          id: 2,
          name: 'Amigurumi Nossa Senhora 🙏',
          description: 'Amigurumi artesanal da Nossa Senhora Aparecida.',
          price: 80.0,
          imageUrl: '/img/nossa-senhora.jpg',
          category: 'Amigurumi',
          stock: 3,
          sizes: null
        },
        {
          id: 3,
          name: 'Caneta Decorada ✏️',
          description: 'Caneta decorada com crochê artesanal colorido.',
          price: 20.0,
          imageUrl: '/img/caneta.jpg',
          category: 'Acessórios',
          stock: 10,
          sizes: null
        }
      ]
    }

    // Hash routing
    this.parseHash()
    window.addEventListener('hashchange', this.parseHash)
  },
  beforeUnmount() {
    window.removeEventListener('hashchange', this.parseHash)
  }
}
</script>

<style>
.blog-list-page {
  padding: 80px 0;
  min-height: 100vh;
  background: #fff5fb;
}

.btn-back-home {
  background: none;
  border: none;
  color: #e91e7b;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
  margin-bottom: 24px;
  display: inline-block;
  font-family: inherit;
}

.btn-back-home:hover { text-decoration: underline; }

.page-title {
  font-size: 2rem;
  color: #1a1a2e;
  font-family: 'Playfair Display', serif;
  margin-bottom: 32px;
}
</style>
