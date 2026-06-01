import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/Login.vue') },
  { path: '/home', component: () => import('../views/Home.vue') },
  { path: '/category', component: () => import('../views/Category.vue') },
  { path: '/detail/:id', component: () => import('../views/Detail.vue') },
  { path: '/me', component: () => import('../views/Me.vue') },
  { path: '/admin', component: () => import('../views/Admin.vue') },
  { path: '/messages', component: () => import('../views/Messages.vue') }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const { useUserStore } = await import('../stores/user')
  const { useAdminStore } = await import('../stores/admin')
  const userStore = useUserStore()
  const adminStore = useAdminStore()

  const isLoginPage = to.path === '/'
  const isAdminPage = to.path === '/admin'
  const isLoggedIn = userStore.isLoggedIn
  const isAdminLoggedIn = adminStore.isAdminLoggedIn

  if (isLoginPage) {
    next()
  } else if (isAdminPage) {
    if (isAdminLoggedIn) {
      next()
    } else {
      next('/')
    }
  } else {
    if (isLoggedIn) {
      next()
    } else {
      next('/')
    }
  }
})

export default router
