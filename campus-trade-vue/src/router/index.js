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

export default router
